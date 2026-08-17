package com.art.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.agentsflex.core.document.Document;
import com.agentsflex.core.model.chat.ChatModel;
import com.agentsflex.core.store.SearchWrapper;
import com.agentsflex.core.store.StoreResult;
import com.agentsflex.store.pgvector.PgvectorVectorStore;
import com.art.DocumentUtil;
import com.art.domain.KnowledgeDoc;
import com.art.domain.KnowledgeDocContent;
import com.art.domain.OssFile;
import com.art.domain.User;
import com.art.domain.vo.KnowledgeDocContentVO;
import com.art.domain.vo.KnowledgeDocVO;
import com.art.domain.vo.OssFileVO;
import com.art.exception.ArtException;
import com.art.mapper.KnowledgeDocContentMapper;
import com.art.mapper.KnowledgeDocMapper;
import com.art.mapper.UserMapper;
import com.art.service.KnowledgeDocService;
import com.art.service.OssFileService;
import com.art.storage.ObjectStorageObject;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.art.utils.SecurityUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 知识库文档服务实现类。
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Slf4j
@Service
public class KnowledgeDocServiceImpl extends ServiceImpl<KnowledgeDocMapper, KnowledgeDoc> implements KnowledgeDocService {

    /**
     * 知识库文档OSS存储目录
     */
    private static final String DIRECTORY = "/knowledge-doc";

    /**
     * 大模型语义分割对文档最大长度限制，超过后先按该长度分割再处理
     */
    private static final int MAX_SPLIT_LENGTH = 10000;

    /**
     * OSS文件服务
     */
    private final OssFileService ossFileService;

    /**
     * 文档内容表Mapper
     */
    private final KnowledgeDocContentMapper knowledgeDocContentMapper;

    /**
     * 用户Mapper（查询上传人名称）
     */
    private final UserMapper userMapper;

    /**
     * AI模型对象（文档语义分割）
     */
    private final ChatModel artChatClient;

    /**
     * 向量数据库
     */
    private final PgvectorVectorStore vectorStore;

    /**
     * 构造函数。
     *
     * @param ossFileService            OSS文件服务
     * @param knowledgeDocContentMapper 文档内容表Mapper
     * @param userMapper                用户Mapper
     * @param artChatClient             AI模型对象
     * @param vectorStore               向量数据库
     */
    public KnowledgeDocServiceImpl(OssFileService ossFileService,
                                   KnowledgeDocContentMapper knowledgeDocContentMapper,
                                   UserMapper userMapper,
                                   ChatModel artChatClient,
                                   PgvectorVectorStore vectorStore) {
        this.ossFileService = ossFileService;
        this.knowledgeDocContentMapper = knowledgeDocContentMapper;
        this.userMapper = userMapper;
        this.artChatClient = artChatClient;
        this.vectorStore = vectorStore;
    }

    /**
     * 上传知识库文档。
     *
     * <p>流程：先调用OSS的上传文件方法将文档上传到对象存储（目录 /knowledge-doc），
     * 然后将文档信息写入知识库文档表（两个状态字段默认均为pending），
     * 插入成功后修改文档表内容解析状态字段（改为processing）并异步执行文档解析，随后返回。</p>
     *
     * @param file 文档
     * @return 文档信息
     */
    @Override
    public KnowledgeDocVO upload(MultipartFile file) {
        // 1. 上传文档到对象存储并记录OSS文件信息
        OssFileVO ossFile = ossFileService.upload(file, DIRECTORY);
        // 2. 文档信息存入知识库文档表，两个状态字段默认pending
        Long userId = SecurityUtil.getUserId();
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setDocName(getFileName(file.getOriginalFilename()));
        doc.setDocType(resolveDocType(file.getOriginalFilename(), file.getContentType()));
        doc.setOssFileId(ossFile.getId());
        doc.setParseStatus(KnowledgeDoc.STATUS_PENDING);
        doc.setVectorStatus(KnowledgeDoc.STATUS_PENDING);
        doc.setUploadTime(LocalDateTime.now());
        doc.setUploadId(userId);
        boolean saved = this.save(doc);
        if (!saved) {
            throw new ArtException("知识库文档信息保存失败");
        }
        return ConvertUtil.convert(doc, KnowledgeDocVO.class);
    }

    /**
     * 分页查询知识库文档信息。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @Override
    public Page<KnowledgeDocVO> queryPage(Page<KnowledgeDocVO> page, KnowledgeDocVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        wrapper.orderBy(KnowledgeDoc::getId, false);
        Page<KnowledgeDocVO> result = this.getMapper().paginateAs(page, wrapper, KnowledgeDocVO.class);
        fillUploadName(result.getRecords());
        return result;
    }

    /**
     * 解析文档。
     *
     * @param id 文档ID
     */
    @Override
    public void parse(Long id) {
        KnowledgeDoc doc = getDocById(id);
        if (KnowledgeDoc.STATUS_PROCESSING.equals(doc.getParseStatus())) {
            throw new ArtException("文档正在解析中，请稍后重试");
        }
        this.asyncParse(id, SecurityUtil.getUserId());
    }

    /**
     * 向量处理文档。
     *
     * @param id 文档ID
     */
    @Override
    public void vectorize(Long id) {
        KnowledgeDoc doc = getDocById(id);
        // 校验当前文档是否已完成文档解析，必须完成解析的才能进行向量处理
        if (!KnowledgeDoc.STATUS_COMPLETE.equals(doc.getParseStatus())) {
            throw new ArtException("请先完成文档解析后再进行向量处理");
        }
        if (KnowledgeDoc.STATUS_PROCESSING.equals(doc.getVectorStatus())) {
            throw new ArtException("文档正在进行向量处理，请耐心等待");
        }
        // 先将向量状态字段改为processing
        KnowledgeDoc update = new KnowledgeDoc();
        update.setId(id);
        update.setVectorStatus(KnowledgeDoc.STATUS_PROCESSING);
        this.updateById(update);
        // 异步执行向量处理
        Thread.ofVirtual().start(() -> doVectorize(id));
    }

    /**
     * 删除文档，同时删除文档内容记录与对象存储中的文件。
     *
     * @param idList 文档ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return false;
        }
        for (Long id : idList) {
            KnowledgeDoc doc = this.getById(id);
            if (doc == null) {
                continue;
            }
            // 删除文档内容记录
            knowledgeDocContentMapper.deleteByQuery(QueryWrapper.create()
                    .eq(KnowledgeDocContent::getDocId, id));
            // 删除文档记录
            boolean removed = this.removeById(id);
            if (!removed) {
                throw new ArtException("知识库文档删除失败");
            }
            // 删除向量
            int dimension = vectorStore.getEmbeddingModel().dimensions();
            if (dimension > 0) {
                float[] placeholderVector = new float[dimension];
                Arrays.fill(placeholderVector, 1.0f);
                SearchWrapper wrapper = new SearchWrapper();
                wrapper.setVector(placeholderVector);
                wrapper.eq("metadata.doc_id", id);
                wrapper.maxResults(Integer.MAX_VALUE);
                List<Document> vectorDocs = vectorStore.search(wrapper);
                if (CollectionUtil.isNotEmpty(vectorDocs)) {
                    vectorStore.delete(vectorDocs.stream().map(Document::getId).collect(Collectors.toList()));
                }
            }
            // 删除对象存储中的文件与OSS文件记录
            if (doc.getOssFileId() != null) {
                ossFileService.delete(List.of(doc.getOssFileId()));
            }
        }
        return true;
    }

    /**
     * 查询文档内容。
     *
     * @param docId 文档ID
     * @return 文档内容
     */
    @Override
    public KnowledgeDocContentVO getContent(Long docId) {
        // 校验文档存在
        getDocById(docId);
        KnowledgeDocContent contentEntity = getContentEntity(docId);
        if (contentEntity == null) {
            return null;
        }
        return ConvertUtil.convert(contentEntity, KnowledgeDocContentVO.class);
    }

    /**
     * 更新文档内容。
     *
     * @param docId   文档ID
     * @param content 新的文档内容
     * @return 更新结果
     */
    @Override
    public Boolean updateContent(Long docId, String content) {
        KnowledgeDoc doc = getDocById(docId);
        // 仅允许对已完成解析的文档编辑内容，避免被后续解析覆盖
        if (!KnowledgeDoc.STATUS_COMPLETE.equals(doc.getParseStatus())) {
            throw new ArtException("请先完成文档解析后再编辑内容");
        }
        KnowledgeDocContent contentEntity = getContentEntity(docId);
        if (contentEntity == null) {
            throw new ArtException("文档内容不存在，请先解析文档");
        }
        KnowledgeDocContent update = new KnowledgeDocContent();
        update.setId(contentEntity.getId());
        update.setContent(content);
        return knowledgeDocContentMapper.update(update) > 0;
    }

    /**
     * 根据文档ID查询最新的文档内容记录。
     *
     * @param docId 文档ID
     * @return 文档内容记录
     */
    private KnowledgeDocContent getContentEntity(Long docId) {
        return knowledgeDocContentMapper.selectOneByQuery(
                QueryWrapper.create()
                        .eq(KnowledgeDocContent::getDocId, docId)
                        .orderBy(KnowledgeDocContent::getId, false));
    }

    /**
     * 修改解析状态为processing并异步执行文档解析。
     *
     * @param id     文档ID
     * @param userId 操作人ID（虚拟线程中上下文不传递，需显式传入）
     */
    private void asyncParse(Long id, Long userId) {
        KnowledgeDoc update = new KnowledgeDoc();
        update.setId(id);
        update.setParseStatus(KnowledgeDoc.STATUS_PROCESSING);
        this.updateById(update);
        Thread.ofVirtual().start(() -> doParse(id, userId));
    }

    /**
     * 异步执行文档解析：下载OSS文件、解析文档内容、存储到文档内容表、
     * 最后将解析状态改为complete，失败改为error。
     *
     * @param id     文档ID
     * @param userId 操作人ID
     */
    private void doParse(Long id, Long userId) {
        LocalDateTime parseStartTime = LocalDateTime.now();
        try {
            KnowledgeDoc doc = getDocById(id);
            // 1. 根据OSS文件表ID去对象存储上下载文档
            OssFile ossFile = ossFileService.getFileById(doc.getOssFileId());
            ObjectStorageObject object = ossFileService.downloadObject(doc.getOssFileId());
            File tempFile = writeTempFile(ossFile.getFileName(), object);
            try {
                // 2. 使用文档解析工具将文档内容处理为String
                String content = DocumentUtil.extract(tempFile);
                // 3. 存储到文档内容表（重新解析时先清除旧内容）
                knowledgeDocContentMapper.deleteByQuery(QueryWrapper.create()
                        .eq(KnowledgeDocContent::getDocId, id));
                KnowledgeDocContent contentEntity = new KnowledgeDocContent();
                contentEntity.setDocId(id);
                contentEntity.setContent(content);
                contentEntity.setParseStartTime(parseStartTime);
                contentEntity.setParseEndTime(LocalDateTime.now());
                contentEntity.setCreateId(userId);
                contentEntity.setCreateTime(LocalDateTime.now());
                contentEntity.setDeleteFlag(0);
                knowledgeDocContentMapper.insert(contentEntity);
                // 4. 解析状态改为complete
                KnowledgeDoc complete = new KnowledgeDoc();
                complete.setId(id);
                complete.setParseStatus(KnowledgeDoc.STATUS_COMPLETE);
                this.updateById(complete);
                log.info("知识库文档解析成功，文档ID：{}", id);
            } finally {
                deleteTempFile(tempFile);
            }
        } catch (Exception e) {
            log.error("知识库文档解析失败，文档ID：{}", id, e);
            KnowledgeDoc error = new KnowledgeDoc();
            error.setId(id);
            error.setParseStatus(KnowledgeDoc.STATUS_ERROR);
            this.updateById(error);
        }
    }

    /**
     * 异步执行向量处理：获取文档内容、分割文档、调用向量库工具处理向量，
     * 最后将向量状态改为complete，失败改为error。
     *
     * @param id 文档ID
     */
    private void doVectorize(Long id) {
        try {
            KnowledgeDoc doc = getDocById(id);
            // 1. 根据知识库文档表ID获取文档内容表中的文档内容
            KnowledgeDocContent contentEntity = getContentEntity(id);
            if (contentEntity == null || StrUtil.isBlank(contentEntity.getContent())) {
                throw new ArtException("文档内容不存在，请先解析文档");
            }
            String documentText = contentEntity.getContent();
            String fileName = doc.getDocName();
            // 2. 分割文档（大模型语义分割对文档最大长度有限制，先按最大长度分割后再循环处理）
            List<Document> documents = new ArrayList<>();
            if (documentText.length() > MAX_SPLIT_LENGTH) {
                log.info("文档内容长度超过最大长度限制，强制分割文档，文档length：{}", documentText.length());
                List<String> docSplit = splitFixedLength(documentText, MAX_SPLIT_LENGTH);
                AtomicInteger chunkNum = new AtomicInteger(1);
                docSplit.forEach(chunk -> {
                    Document document = Document.of(chunk);
                    document.setTitle(fileName);
                    log.info("开始分割文档，文档ID：{}，第{}个", id, chunkNum);
                    documents.addAll(DocumentUtil.splitAi(document, artChatClient));
                    chunkNum.getAndIncrement();
                });
            } else {
                Document document = Document.of(documentText);
                document.setTitle(fileName);
                log.info("开始分割文档，文档ID：{}", id);
                documents.addAll(DocumentUtil.splitAi(document, artChatClient));
            }
            // 3. 调用向量库工具处理向量
            log.info("开始向量化文档，文档ID：{}", id);
            // 查询该文档是否已经存在向量，如果存在，先删除旧的
            int dimension = vectorStore.getEmbeddingModel().dimensions();
            if (dimension > 0) {
                float[] placeholderVector = new float[dimension];
                Arrays.fill(placeholderVector, 1.0f);
                SearchWrapper wrapper = new SearchWrapper();
                wrapper.setVector(placeholderVector);
                wrapper.eq("metadata.doc_id", id);
                wrapper.maxResults(Integer.MAX_VALUE);
                List<Document> vectorDocs = vectorStore.search(wrapper);
                if (CollectionUtil.isNotEmpty(vectorDocs)) {
                    vectorStore.delete(vectorDocs.stream().map(Document::getId).collect(Collectors.toList()));
                }
            }
            // 添加元数据信息
            for (Document document : documents) {
                document.putMetadata("doc_id", id);
            }
            StoreResult store = vectorStore.store(documents);
            if (store.getException() != null) {
                throw new ArtException("文档向量化失败：", store.getException());
            }
            // 4. 向量状态改为complete
            KnowledgeDoc complete = new KnowledgeDoc();
            complete.setId(id);
            complete.setVectorStatus(KnowledgeDoc.STATUS_COMPLETE);
            this.updateById(complete);
            log.info("知识库文档向量处理成功，文档ID：{}", id);
        } catch (Exception e) {
            log.error("知识库文档向量处理失败，文档ID：{}", id, e);
            KnowledgeDoc error = new KnowledgeDoc();
            error.setId(id);
            error.setVectorStatus(KnowledgeDoc.STATUS_ERROR);
            this.updateById(error);
        }
    }

    /**
     * 根据ID获取知识库文档。
     *
     * @param id 文档ID
     * @return 文档信息
     */
    private KnowledgeDoc getDocById(Long id) {
        KnowledgeDoc doc = this.getById(id);
        if (doc == null) {
            throw new ArtException("知识库文档不存在");
        }
        return doc;
    }

    /**
     * 将OSS下载的对象写入临时文件（保留原文件扩展名，供文档解析工具识别）。
     *
     * @param originalFileName 原始文件名
     * @param object           OSS下载对象
     * @return 临时文件
     */
    private File writeTempFile(String originalFileName, ObjectStorageObject object) {
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            String ext = "";
            if (StrUtil.isNotBlank(originalFileName)) {
                int index = originalFileName.lastIndexOf('.');
                if (index >= 0) {
                    ext = originalFileName.substring(index).toLowerCase(Locale.ROOT);
                }
            }
            File targetFile = new File(tempDir + File.separator
                    + UUID.randomUUID().toString().replace("-", "") + ext);
            try (var input = object.inputStream()) {
                Files.copy(input, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return targetFile;
        } catch (IOException e) {
            throw new ArtException("文档下载写入临时文件失败：", e);
        }
    }

    /**
     * 删除临时文件。
     *
     * @param tempFile 临时文件
     */
    private void deleteTempFile(File tempFile) {
        try {
            Files.deleteIfExists(tempFile.toPath());
        } catch (IOException e) {
            log.warn("删除临时文件失败：{}", tempFile.getAbsolutePath(), e);
        }
    }

    /**
     * 文档长度大于语义分割要求最大长度时，根据最大长度分割文档。
     *
     * @param documentText 文档内容
     * @param chunkSize    最大长度
     * @return 分割后的文档列表
     */
    public static List<String> splitFixedLength(String documentText, int chunkSize) {
        int len = documentText.length();
        List<String> chunks = new ArrayList<>((len + chunkSize - 1) / chunkSize);
        for (int i = 0; i < len; i += chunkSize) {
            int end = Math.min(len, i + chunkSize);
            chunks.add(documentText.substring(i, end));
        }
        return chunks;
    }

    /**
     * 去除原始文件名中的路径信息。
     *
     * @param originalFilename 原始文件名
     * @return 文件名
     */
    private String getFileName(String originalFilename) {
        if (StrUtil.isBlank(originalFilename)) {
            return "file";
        }
        String name = originalFilename.replace('\\', '/');
        int index = name.lastIndexOf('/');
        return index >= 0 ? name.substring(index + 1) : name;
    }

    /**
     * 解析文档类型：优先取文件扩展名，取不到时回退为Content-Type。
     *
     * @param originalFilename 原始文件名
     * @param contentType      文件Content-Type
     * @return 文档类型
     */
    private String resolveDocType(String originalFilename, String contentType) {
        if (StrUtil.isNotBlank(originalFilename)) {
            int index = originalFilename.lastIndexOf('.');
            if (index >= 0 && index < originalFilename.length() - 1) {
                return originalFilename.substring(index + 1).toLowerCase(Locale.ROOT);
            }
        }
        return contentType;
    }

    /**
     * 补充上传人名称。
     *
     * @param records 分页数据
     */
    private void fillUploadName(List<KnowledgeDocVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> userIds = records.stream()
                .map(KnowledgeDocVO::getUploadId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) {
            return;
        }
        List<User> users = userMapper.selectListByQuery(QueryWrapper.create().in(User::getId, userIds));
        Map<Long, String> nameMap = users.stream().collect(Collectors.toMap(
                User::getId,
                user -> StrUtil.isNotBlank(user.getNickName()) ? user.getNickName() : user.getUserName()));
        records.forEach(record -> record.setUploadName(nameMap.get(record.getUploadId())));
    }
}

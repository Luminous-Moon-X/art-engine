package com.art.service;

import com.art.domain.KnowledgeDoc;
import com.art.domain.vo.KnowledgeDocVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文档服务。
 *
 * @author Luminous.X
 * @since 1.3.1
 */
public interface KnowledgeDocService extends IService<KnowledgeDoc> {

    /**
     * 上传知识库文档。
     *
     * <p>先将文档上传到对象存储（目录 /knowledge-doc），随后写入知识库文档表，
     * 两个状态字段默认为待处理，插入成功后修改内容解析状态并异步执行文档解析。</p>
     *
     * @param file 文档
     * @return 文档信息
     */
    KnowledgeDocVO upload(MultipartFile file);

    /**
     * 分页查询知识库文档信息。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    Page<KnowledgeDocVO> queryPage(Page<KnowledgeDocVO> page, KnowledgeDocVO vo);

    /**
     * 解析文档。
     *
     * <p>先将解析状态改为processing，然后异步执行：根据OSS文件表ID下载文档、
     * 解析文档内容、存储到文档内容表，最后将解析状态改为complete（失败改为error）。</p>
     *
     * @param id 文档ID
     */
    void parse(Long id);

    /**
     * 向量处理文档。
     *
     * <p>先校验文档解析状态必须为complete，然后将向量状态改为processing，
     * 异步执行：读取文档内容、分割文档、调用向量库工具处理向量，
     * 最后将向量状态改为complete（失败改为error）。</p>
     *
     * @param id 文档ID
     */
    void vectorize(Long id);

    /**
     * 删除文档，同时删除文档内容记录与对象存储中的文件。
     *
     * @param idList 文档ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);
}

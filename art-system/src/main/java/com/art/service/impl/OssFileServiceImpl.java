package com.art.service.impl;

import cn.hutool.core.util.StrUtil;
import com.art.domain.OssFile;
import com.art.domain.vo.OssFileVO;
import com.art.exception.ArtException;
import com.art.mapper.OssFileMapper;
import com.art.service.OssFileService;
import com.art.storage.ObjectStorageObject;
import com.art.storage.ObjectStorageService;
import com.art.storage.ObjectStorageServiceManager;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 对象存储上传文件服务实现。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Service
public class OssFileServiceImpl extends ServiceImpl<OssFileMapper, OssFile> implements OssFileService {

    /**
     * 对象存储服务管理器
     */
    private final ObjectStorageServiceManager objectStorageServiceManager;

    /**
     * 构造函数。
     *
     * @param objectStorageServiceManager 对象存储服务管理器
     */
    public OssFileServiceImpl(ObjectStorageServiceManager objectStorageServiceManager) {
        this.objectStorageServiceManager = objectStorageServiceManager;
    }

    /**
     * 上传文件并记录文件信息。
     *
     * @param file      文件
     * @param directory 目录
     * @return 文件信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OssFileVO upload(MultipartFile file, String directory) {
        if (file == null || file.isEmpty()) {
            throw new ArtException("上传文件不能为空");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new ArtException("读取上传文件失败：" + e.getMessage());
        }

        String fileName = getFileName(file.getOriginalFilename());
        String objectKey = generateObjectKey(fileName, directory);
        String contentType = file.getContentType();
        String fileMd5 = DigestUtils.md5DigestAsHex(bytes);
        String url = objectStorageServiceManager.getUrl(objectKey);
        Long configId = objectStorageServiceManager.getConfigId();

        objectStorageServiceManager.upload(
                objectKey,
                new ByteArrayInputStream(bytes),
                contentType,
                bytes.length
        );

        OssFile entity = new OssFile();
        entity.setOssConfigId(configId);
        entity.setFileName(fileName);
        entity.setObjectKey(objectKey);
        entity.setContentType(contentType);
        entity.setFileSize((long) bytes.length);
        entity.setFileMd5(fileMd5);
        entity.setUrl(url);

        try {
            boolean saved = this.save(entity);
            if (!saved) {
                throw new ArtException("上传文件信息保存失败");
            }
        } catch (Exception e) {
            objectStorageServiceManager.delete(objectKey);
            throw e;
        }
        return ConvertUtil.convert(entity, OssFileVO.class);
    }

    /**
     * 根据ID获取文件信息。
     *
     * @param id 文件ID
     * @return 文件信息
     */
    @Override
    public OssFile getFileById(Long id) {
        OssFile file = this.getById(id);
        if (file == null) {
            throw new ArtException("上传文件信息不存在");
        }
        return file;
    }

    /**
     * 根据文件ID下载对象。
     *
     * <p>按文件自身记录的OSS配置下载：文件可能是由全局配置或历史配置上传的，
     * 使用当前租户的配置会导致下载到错误的桶。</p>
     *
     * @param id 文件ID
     * @return 对象内容
     */
    @Override
    public ObjectStorageObject downloadObject(Long id) {
        OssFile file = getFileById(id);
        return objectStorageServiceManager.serviceForConfig(file.getOssConfigId()).download(file.getObjectKey());
    }

    /**
     * 分页查询文件信息。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @Override
    public Page<OssFileVO> queryPage(Page<OssFileVO> page, OssFileVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        wrapper.orderBy(OssFile::getId, false);
        return this.getMapper().paginateAs(page, wrapper, OssFileVO.class);
    }

    /**
     * 删除文件，同时删除文件信息和对象存储中的文件。
     *
     * @param idList 文件ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return false;
        }
        for (Long id : idList) {
            OssFile file = this.getById(id);
            if (file == null) {
                continue;
            }
            // 文件对应的配置可能为全局/历史配置，按文件自身配置读取
            ObjectStorageService service = objectStorageServiceManager.serviceForConfig(file.getOssConfigId());
            service.delete(file.getObjectKey());
            boolean removed = this.removeById(id);
            if (!removed) {
                throw new ArtException("文件信息删除失败");
            }
        }
        return true;
    }

    /**
     * 生成对象Key。
     *
     * @param fileName  原始文件名
     * @param directory 目录
     * @return 对象Key
     */
    private String generateObjectKey(String fileName, String directory) {
        String dir = StrUtil.isBlank(directory)
                ? ""
                : directory.trim().replaceAll("^[/\\\\]+|[/\\\\]+$", "") + "/";
        String ext = "";
        int index = fileName == null ? -1 : fileName.lastIndexOf('.');
        if (index >= 0) {
            ext = fileName.substring(index).toLowerCase(Locale.ROOT);
        }
        int year = LocalDate.now().getYear();
        int month = LocalDate.now().getMonthValue();
        int day = LocalDate.now().getDayOfMonth();
        return dir + year + "/" + month + "/" + day + "/" + UUID.randomUUID().toString().replace("-", "") + ext;
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
}

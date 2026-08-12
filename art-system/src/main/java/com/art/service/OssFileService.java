package com.art.service;

import com.art.domain.OssFile;
import com.art.domain.vo.OssFileVO;
import com.art.storage.ObjectStorageObject;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 对象存储上传文件服务。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
public interface OssFileService extends IService<OssFile> {

    /**
     * 上传文件并记录文件信息。
     *
     * @param file      文件
     * @param directory 目录
     * @return 文件信息
     */
    OssFileVO upload(MultipartFile file, String directory);

    /**
     * 根据ID获取文件信息。
     *
     * @param id 文件ID
     * @return 文件信息
     */
    OssFile getFileById(Long id);

    /**
     * 根据文件ID下载对象。
     *
     * @param id 文件ID
     * @return 对象内容
     */
    ObjectStorageObject downloadObject(Long id);

    /**
     * 分页查询文件信息。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    Page<OssFileVO> queryPage(Page<OssFileVO> page, OssFileVO vo);

    /**
     * 删除文件，同时删除文件信息和对象存储中的文件。
     *
     * @param idList 文件ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);
}

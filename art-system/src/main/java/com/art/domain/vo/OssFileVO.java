package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对象存储上传文件VO。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Data
public class OssFileVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 对象存储配置ID
     */
    @Query(type = Query.Type.EQ)
    private Long ossConfigId;

    /**
     * 原始文件名
     */
    @Query(type = Query.Type.LIKE)
    private String fileName;

    /**
     * 对象存储Key
     */
    private String objectKey;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件MD5
     */
    private String fileMd5;

    /**
     * 访问地址
     */
    private String url;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

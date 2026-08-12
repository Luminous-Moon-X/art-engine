package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对象存储上传文件信息实体类。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_sys_oss_file")
public class OssFile extends BaseEntity {

    /**
     * 对象存储配置ID
     */
    @Column("oss_config_id")
    private Long ossConfigId;

    /**
     * 原始文件名
     */
    @Column("file_name")
    private String fileName;

    /**
     * 对象存储Key
     */
    @Column("object_key")
    private String objectKey;

    /**
     * 文件类型
     */
    @Column("content_type")
    private String contentType;

    /**
     * 文件大小（字节）
     */
    @Column("file_size")
    private Long fileSize;

    /**
     * 文件MD5
     */
    @Column("file_md5")
    private String fileMd5;

    /**
     * 访问地址
     */
    @Column("url")
    private String url;
}

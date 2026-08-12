package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 对象存储配置实体类。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_sys_oss_config")
public class OssConfig extends BaseEntity {

    /**
     * 配置名称
     */
    @Column("config_name")
    private String configName;

    /**
     * S3兼容服务地址
     */
    @Column("endpoint")
    private String endpoint;

    /**
     * Access Key
     */
    @Column("access_key")
    private String accessKey;

    /**
     * Secret Key（加密存储）
     */
    @Column("secret_key")
    private String secretKey;

    /**
     * Bucket名称
     */
    @Column("bucket_name")
    private String bucketName;

    /**
     * 启用标识：0-禁用 1-启用
     */
    @Column("enable_flag")
    private Integer enableFlag;

    /**
     * 备注
     */
    @Column("remark")
    private String remark;
}

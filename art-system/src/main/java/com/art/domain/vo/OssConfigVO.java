package com.art.domain.vo;

import com.art.annotation.Query;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对象存储配置VO。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Data
public class OssConfigVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 配置名称
     */
    @Query(type = Query.Type.LIKE)
    private String configName;

    /**
     * S3兼容服务地址
     */
    private String endpoint;

    /**
     * Access Key
     */
    private String accessKey;

    /**
     * Secret Key，仅接收前端提交，不参与响应序列化
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String secretKey;

    /**
     * Bucket名称
     */
    @Query(type = Query.Type.LIKE)
    private String bucketName;

    /**
     * 启用标识：0-禁用 1-启用
     */
    @Query(type = Query.Type.EQ)
    private Integer enableFlag;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

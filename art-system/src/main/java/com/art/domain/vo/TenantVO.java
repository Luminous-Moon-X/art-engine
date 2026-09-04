package com.art.domain.vo;

import com.art.annotation.Query;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户VO（分页查询/新增/编辑）
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@Data
public class TenantVO {
    /**
     * 租户ID
     */
    private Long id;
    /**
     * 租户编码
     */
    @Query(type = Query.Type.LIKE)
    private String tenantCode;
    /**
     * 租户名称
     */
    @Query(type = Query.Type.LIKE)
    private String tenantName;
    /**
     * 租户套餐ID
     */
    @Query(type = Query.Type.EQ)
    private Long packageId;
    /**
     * 租户套餐名称（查询结果）
     */
    private String packageName;
    /**
     * 启用标识
     */
    private Integer enableFlag;
    /**
     * 到期时间
     */
    private LocalDate expireDate;
    /**
     * 备注
     */
    private String remark;
    /**
     * 管理员用户名（新增租户时必须填写，用于创建该租户的管理员账号）
     */
    private String adminUsername;
    /**
     * 管理员昵称（创建租户时默认使用租户名称）
     */
    @JsonProperty("adminNickName")
    private String adminNickname;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
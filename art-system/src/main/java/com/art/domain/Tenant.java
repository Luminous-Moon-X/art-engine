package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 租户实体类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@Table("p_sys_tenant")
@EqualsAndHashCode(callSuper = true)
public class Tenant extends BaseEntity {
    /**
     * 租户名称
     */
    @Column("tenant_name")
    private String tenantName;
    /**
     * 过期时间
     */
    @Column("expire_time")
    private LocalDateTime expireTime;
    /**
     * 启用标志
     */
    @Column("enable_flag")
    private Integer enableFlag;
}

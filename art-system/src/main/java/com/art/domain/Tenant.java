package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 租户实体类（系统级数据，表无 tenant_id 列，操作时必须在系统级作用域内）
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_sys_tenant")
public class Tenant extends BaseEntity {
    /**
     * 租户编码
     */
    @Column("tenant_code")
    private String tenantCode;
    /**
     * 租户名称
     */
    @Column("tenant_name")
    private String tenantName;
    /**
     * 租户套餐ID
     */
    @Column("package_id")
    private Long packageId;
    /**
     * 启用标识
     */
    @Column("enable_flag")
    private Integer enableFlag;
    /**
     * 到期时间
     */
    @Column("expire_date")
    private LocalDate expireDate;
    /**
     * 备注
     */
    @Column("remark")
    private String remark;
}
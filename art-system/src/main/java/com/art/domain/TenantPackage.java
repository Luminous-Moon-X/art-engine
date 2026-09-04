package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户套餐实体类（系统级数据，表无 tenant_id 列，操作时必须在系统级作用域内）
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_sys_tenant_package")
public class TenantPackage extends BaseEntity {
    /**
     * 套餐名称
     */
    @Column("package_name")
    private String packageName;
    /**
     * 菜单权限标识集合（逗号分隔，NULL或空=不限制菜单范围）
     */
    @Column("permission_signs")
    private String permissionSigns;
    /**
     * 启用标识
     */
    @Column("enable_flag")
    private Integer enableFlag;
    /**
     * 备注
     */
    @Column("remark")
    private String remark;
}
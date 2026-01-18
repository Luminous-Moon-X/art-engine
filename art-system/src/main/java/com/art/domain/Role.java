package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色表
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
@Table("p_sys_role")
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {
    /**
     * 角色名称
     */
    @Column("role_name")
    private String roleName;
    /**
     * 角色编码
     */
    @Column("role_code")
    private String roleCode;
    /**
     * 角色描述
     */
    @Column("role_description")
    private String roleDescription;
    /**
     * 是否启用
     */
    @Column("enable_flag")
    private Boolean enableFlag;
}

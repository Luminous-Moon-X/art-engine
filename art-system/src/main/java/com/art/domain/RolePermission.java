package com.art.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 角色权限表
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Data
@Table("p_sys_role_permission")
public class RolePermission {
    /**
     * 主键
     */
    @Id
    @Column("id")
    private Long id;
    /**
     * 角色id
     */
    @Column("role_id")
    private Long roleId;
    /**
     * 权限标识
     */
    @Column("permission_sign")
    private String permissionSign;
}

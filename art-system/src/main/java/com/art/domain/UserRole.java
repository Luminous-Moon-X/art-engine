package com.art.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 用户角色关联表
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
@Table("p_sys_user_role")
public class UserRole {
    /**
     * 主键
     */
    @Id
    @Column("id")
    private Long id;
    /**
     * 用户ID
     */
    @Column("user_id")
    private Long userId;
    /**
     * 角色ID
     */
    @Column("role_id")
    private Long roleId;
}

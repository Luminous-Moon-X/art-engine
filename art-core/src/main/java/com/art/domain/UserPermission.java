package com.art.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户权限表
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Data
@Table("p_sys_user_permission")
@AllArgsConstructor
@NoArgsConstructor
public class UserPermission {
    /**
     * 主键
     */
    @Id
    @Column("id")
    private Long id;
    /**
     * 用户id
     */
    @Column("user_id")
    private Long userId;
    /**
     * 权限标识
     */
    @Column("permission_sign")
    private String permissionSign;
}

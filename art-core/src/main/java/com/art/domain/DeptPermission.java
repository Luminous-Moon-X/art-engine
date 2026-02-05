package com.art.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门权限表
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Data
@Table("p_sys_dept_permission")
@AllArgsConstructor
@NoArgsConstructor
public class DeptPermission {
    /**
     * 主键
     */
    @Id
    @Column("id")
    private Long id;
    /**
     * 部门id
     */
    @Column("dept_id")
    private Long deptId;
    /**
     * 权限标识
     */
    @Column("permission_sign")
    private String permissionSign;
}

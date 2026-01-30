package com.art.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单权限信息
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuPermissionVO {
    /**
     * 角色权限
     */
    private List<String> rolePermission;
    /**
     * 部门权限
     */
    private List<String> deptPermission;
    /**
     * 用户权限
     */
    private List<String> userPermission;
}

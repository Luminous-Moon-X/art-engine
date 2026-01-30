package com.art.service;

import com.art.domain.vo.MenuPermissionVO;

/**
 * 菜单权限服务
 *
 * @author Luminous.X
 * @since 1.1.0
 */
public interface MenuPermissionService {
    /**
     * 获取菜单权限信息
     *
     * @return 菜单权限信息
     */
    MenuPermissionVO getMenuPermission();
}

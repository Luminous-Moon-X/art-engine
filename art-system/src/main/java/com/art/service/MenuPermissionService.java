package com.art.service;

import com.art.domain.vo.MenuPermissionVO;

import java.util.List;

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
    List<String> getMenuPermission(String type, Long id);

    /**
     * 设置菜单权限信息
     *
     * @param vo 菜单权限信息
     * @return 设置结果
     */
    Boolean setPermission(MenuPermissionVO vo);
}

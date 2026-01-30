package com.art.service.impl;

import com.art.cache.DeptPermissionCache;
import com.art.cache.RolePermissionCache;
import com.art.cache.UserPermissionCache;
import com.art.domain.DeptPermission;
import com.art.domain.RolePermission;
import com.art.domain.UserPermission;
import com.art.domain.vo.MenuPermissionVO;
import com.art.service.MenuPermissionService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜单权限服务实现类
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Service
public class MenuPermissionServiceImpl implements MenuPermissionService {
    /**
     * 用户功能权限缓存
     */
    private final UserPermissionCache userPermissionCache;
    /**
     * 角色功能权限缓存
     */
    private final RolePermissionCache rolePermissionCache;
    /**
     * 部门功能权限缓存
     */
    private final DeptPermissionCache deptPermissionCache;

    /**
     * 构造函数
     *
     * @param userPermissionCache 用户功能权限缓存
     * @param rolePermissionCache 角色功能权限缓存
     * @param deptPermissionCache 部门功能权限缓存
     */
    public MenuPermissionServiceImpl(UserPermissionCache userPermissionCache, RolePermissionCache rolePermissionCache, DeptPermissionCache deptPermissionCache) {
        this.userPermissionCache = userPermissionCache;
        this.rolePermissionCache = rolePermissionCache;
        this.deptPermissionCache = deptPermissionCache;
    }

    /**
     * 获取菜单权限
     *
     * @return 菜单权限
     */
    @Override
    public MenuPermissionVO getMenuPermission() {
        // 获取当前登录用户权限，包括用户、部门、角色功能权限
        List<String> userPermissionSignList = this.userPermissionCache.getByCurrentUser()
                .stream().map(UserPermission::getPermissionSign)
                .toList();
        List<String> rolePermissionSignList = this.rolePermissionCache.getByCurrentRole()
                .stream().map(RolePermission::getPermissionSign)
                .toList();
        List<String> deptPermissionSignList = this.deptPermissionCache.getByCurrentDept()
                .stream().map(DeptPermission::getPermissionSign)
                .toList();
        return new MenuPermissionVO(userPermissionSignList, rolePermissionSignList, deptPermissionSignList);
    }
}

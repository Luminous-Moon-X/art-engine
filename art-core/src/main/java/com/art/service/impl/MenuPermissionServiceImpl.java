package com.art.service.impl;

import com.art.cache.*;
import com.art.domain.DeptPermission;
import com.art.domain.RolePermission;
import com.art.domain.UserPermission;
import com.art.domain.vo.MenuPermissionVO;
import com.art.exception.ArtException;
import com.art.mapper.DeptPermissionMapper;
import com.art.mapper.RolePermissionMapper;
import com.art.mapper.UserPermissionMapper;
import com.art.service.MenuPermissionService;
import com.art.utils.SecurityUtil;
import com.art.utils.StringUtil;
import com.mybatisflex.core.query.QueryWrapper;
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
     * 角色功能权限Mapper
     */
    private final RolePermissionMapper rolePermissionMapper;
    /**
     * 用户功能权限Mapper
     */
    private final UserPermissionMapper userPermissionMapper;
    /**
     * 部门功能权限Mapper
     */
    private final DeptPermissionMapper deptPermissionMapper;
    /**
     * 菜单权限标识缓存
     */
    private final MenuAuthCache menuAuthCache;

    /**
     * 构造函数
     *
     * @param userPermissionCache  用户功能权限缓存
     * @param rolePermissionCache  角色功能权限缓存
     * @param deptPermissionCache  部门功能权限缓存
     * @param rolePermissionMapper 角色功能权限Mapper
     * @param deptPermissionMapper 部门功能权限Mapper
     * @param userPermissionMapper 用户功能权限Mapper
     * @param menuAuthCache        菜单权限标识缓存
     */
    public MenuPermissionServiceImpl(UserPermissionCache userPermissionCache, RolePermissionCache rolePermissionCache,
                                     DeptPermissionCache deptPermissionCache, RolePermissionMapper rolePermissionMapper,
                                     UserPermissionMapper userPermissionMapper, DeptPermissionMapper deptPermissionMapper, MenuAuthCache menuAuthCache) {
        this.userPermissionCache = userPermissionCache;
        this.rolePermissionCache = rolePermissionCache;
        this.deptPermissionCache = deptPermissionCache;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userPermissionMapper = userPermissionMapper;
        this.deptPermissionMapper = deptPermissionMapper;
        this.menuAuthCache = menuAuthCache;
    }

    /**
     * 获取菜单权限
     *
     * @return 菜单权限
     */
    @Override
    public List<String> getMenuPermission(String type, Long id) {
        List<String> permissionSignList;
        if ("admin".equals(SecurityUtil.getUserType()) && StringUtil.isBlank(type)) {
            return this.menuAuthCache.get();
        }
        if (StringUtil.isNotBlank(type)) {
            // 获取指定用户、角色或部门的功能权限
            permissionSignList = switch (type) {
                case "user" -> this.userPermissionCache.getByUserId(id)
                        .stream().map(UserPermission::getPermissionSign)
                        .toList();
                case "role" -> this.rolePermissionCache.getByRoleId(id)
                        .stream().map(RolePermission::getPermissionSign)
                        .toList();
                case "dept" -> this.deptPermissionCache.getByDeptId(id)
                        .stream().map(DeptPermission::getPermissionSign)
                        .toList();
                default -> throw new ArtException("权限类型错误，请联系管理员！");
            };
        } else {
            // 获取当前登录用户权限，包括用户、部门、角色功能权限
            permissionSignList = new java.util.ArrayList<>(this.userPermissionCache.getByCurrentUser()
                    .stream().map(UserPermission::getPermissionSign)
                    .toList());
            List<String> rolePermissionSignList = this.rolePermissionCache.getByCurrentRole()
                    .stream().map(RolePermission::getPermissionSign)
                    .toList();
            List<String> deptPermissionSignList = this.deptPermissionCache.getByCurrentDept()
                    .stream().map(DeptPermission::getPermissionSign)
                    .toList();
            permissionSignList.addAll(rolePermissionSignList);
            permissionSignList.addAll(deptPermissionSignList);
        }
        return permissionSignList;
    }

    /**
     * 设置菜单权限
     *
     * @param vo 菜单权限信息
     * @return 是否成功
     */
    @Override
    public Boolean setPermission(MenuPermissionVO vo) {
        String type = vo.getType();
        Long id = vo.getId();
        List<String> permissionSignList = vo.getPermissionSignList();
        switch (type) {
            case "user" -> {
                this.userPermissionMapper.deleteByQuery(QueryWrapper.create().eq(UserPermission::getUserId, id));
                if (!permissionSignList.isEmpty()) {
                    List<UserPermission> userPermissions = permissionSignList.stream()
                            .map(permissionSign -> new UserPermission(null, id, permissionSign))
                            .toList();
                    if (this.userPermissionMapper.insertBatch(userPermissions) <= 0) {
                        throw new ArtException("用户功能权限保存失败，请联系管理员！");
                    }
                }
                Thread.ofVirtual().start(userPermissionCache::init);
            }
            case "role" -> {
                this.rolePermissionMapper.deleteByQuery(QueryWrapper.create().eq(RolePermission::getRoleId, id));
                if (!permissionSignList.isEmpty()) {
                    List<RolePermission> rolePermissions = permissionSignList.stream()
                            .map(permissionSign -> new RolePermission(null, id, permissionSign))
                            .toList();
                    if (this.rolePermissionMapper.insertBatch(rolePermissions) <= 0) {
                        throw new ArtException("角色功能权限保存失败，请联系管理员！");
                    }
                }
                Thread.ofVirtual().start(rolePermissionCache::init);
            }
            case "dept" -> {
                this.deptPermissionMapper.deleteByQuery(QueryWrapper.create().eq(DeptPermission::getDeptId, id));
                if (!permissionSignList.isEmpty()) {
                    List<DeptPermission> deptPermissions = permissionSignList.stream()
                            .map(permissionSign -> new DeptPermission(null, id, permissionSign))
                            .toList();
                    if (this.deptPermissionMapper.insertBatch(deptPermissions) <= 0) {
                        throw new ArtException("部门功能权限保存失败，请联系管理员！");
                    }
                }
                Thread.ofVirtual().start(deptPermissionCache::init);
            }
            default -> throw new ArtException("权限类型错误，请联系管理员！");
        }
        return true;
    }

    /**
     * 删除菜单权限
     *
     * @param menuPermissionSign 菜单权限标识
     */
    @Override
    public void deletePermissionByMenu(List<String> menuPermissionSign) {
        this.deptPermissionMapper.deleteByQuery(QueryWrapper.create().in(DeptPermission::getPermissionSign, menuPermissionSign));
        this.rolePermissionMapper.deleteByQuery(QueryWrapper.create().in(RolePermission::getPermissionSign, menuPermissionSign));
        this.userPermissionMapper.deleteByQuery(QueryWrapper.create().in(UserPermission::getPermissionSign, menuPermissionSign));
    }
}

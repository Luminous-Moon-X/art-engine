package com.art.service.impl;

import com.art.auth.cache.DeptPermissionCache;
import com.art.auth.cache.MenuAuthCache;
import com.art.auth.cache.RolePermissionCache;
import com.art.auth.cache.UserPermissionCache;
import com.art.cache.support.CacheRefreshService;
import com.art.domain.DeptPermission;
import com.art.domain.RolePermission;
import com.art.domain.UserPermission;
import com.art.domain.vo.MenuPermissionVO;
import com.art.exception.ArtException;
import com.art.mapper.DeptPermissionMapper;
import com.art.mapper.RolePermissionMapper;
import com.art.mapper.UserPermissionMapper;
import com.art.service.MenuPermissionService;
import com.art.tenant.TenantPermissionProvider;
import com.art.tenant.TenantSubjectValidator;
import com.art.tenant.TenantSupport;
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
     * 缓存刷新服务
     */
    private final CacheRefreshService cacheRefreshService;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;
    /**
     * 当前租户菜单范围提供者
     */
    private final TenantPermissionProvider tenantPermissionProvider;
    /**
     * 授权主体租户归属校验
     */
    private final TenantSubjectValidator tenantSubjectValidator;

    /**
     * 构造函数
     *
     * @param userPermissionCache      用户功能权限缓存
     * @param rolePermissionCache      角色功能权限缓存
     * @param deptPermissionCache      部门功能权限缓存
     * @param rolePermissionMapper     角色功能权限Mapper
     * @param deptPermissionMapper     部门功能权限Mapper
     * @param userPermissionMapper     用户功能权限Mapper
     * @param menuAuthCache            菜单权限标识缓存
     * @param cacheRefreshService      缓存刷新服务
     * @param tenantSupport            多租户支持
     * @param tenantPermissionProvider 当前租户菜单范围提供者
     * @param tenantSubjectValidator   授权主体租户归属校验
     */
    public MenuPermissionServiceImpl(UserPermissionCache userPermissionCache, RolePermissionCache rolePermissionCache,
                                     DeptPermissionCache deptPermissionCache, RolePermissionMapper rolePermissionMapper,
                                     UserPermissionMapper userPermissionMapper, DeptPermissionMapper deptPermissionMapper,
                                     MenuAuthCache menuAuthCache, CacheRefreshService cacheRefreshService,
                                     TenantSupport tenantSupport, TenantPermissionProvider tenantPermissionProvider,
                                     TenantSubjectValidator tenantSubjectValidator) {
        this.userPermissionCache = userPermissionCache;
        this.rolePermissionCache = rolePermissionCache;
        this.deptPermissionCache = deptPermissionCache;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userPermissionMapper = userPermissionMapper;
        this.deptPermissionMapper = deptPermissionMapper;
        this.menuAuthCache = menuAuthCache;
        this.cacheRefreshService = cacheRefreshService;
        this.tenantSupport = tenantSupport;
        this.tenantPermissionProvider = tenantPermissionProvider;
        this.tenantSubjectValidator = tenantSubjectValidator;
    }

    /**
     * 获取菜单权限
     *
     * @return 菜单权限
     */
    @Override
    public List<String> getMenuPermission(String type, Long id) {
        String userType = SecurityUtil.getUserType();
        // 超级管理员：全部菜单权限标识
        if (tenantSupport.isSuperAdmin(userType) && StringUtil.isBlank(type)) {
            return this.menuAuthCache.get();
        }
        // 租户管理员：默认拥有全部权限；多租户开启时收敛到套餐菜单范围
        // 平台级管理菜单权限标识一律剔除，不可下发给租户
        if (tenantSupport.isTenantAdmin(userType) && StringUtil.isBlank(type)) {
            if (tenantSupport.isEnable()) {
                List<String> packageSigns = this.tenantPermissionProvider.getCurrentTenantPermissionSigns();
                if (packageSigns.isEmpty()) {
                    return tenantSupport.filterPlatformMenuSigns(this.menuAuthCache.get());
                }
                return tenantSupport.filterPlatformMenuSigns(packageSigns);
            }
            return tenantSupport.filterPlatformMenuSigns(this.menuAuthCache.get());
        }
        if (StringUtil.isNotBlank(type)) {
            // 获取指定用户、角色或部门的功能权限
            return switch (type) {
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
        }
        // 获取当前登录用户权限，包括用户、部门、角色功能权限
        List<String> permissionSignList = new java.util.ArrayList<>(this.userPermissionCache.getByCurrentUser()
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
        // 多租户开启：当前租户菜单范围不得超过其套餐
        if (tenantSupport.isEnable()) {
            List<String> packageSigns = this.tenantPermissionProvider.getCurrentTenantPermissionSigns();
            if (!packageSigns.isEmpty()) {
                permissionSignList.retainAll(packageSigns);
            }
        }
        // 平台级管理菜单权限标识一律剔除，不可下发给租户
        return tenantSupport.filterPlatformMenuSigns(permissionSignList);
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
        // 授权主体（角色/用户/部门）必须属于当前生效租户，防止跨租户授权
        this.tenantSubjectValidator.validateAssignable(type, id);
        // 多租户开启：非超级管理员只能分配套餐范围内的菜单权限
        if (tenantSupport.isEnable() && !tenantSupport.isSuperAdmin(SecurityUtil.getUserType())) {
            List<String> packageSigns = this.tenantPermissionProvider.getCurrentTenantPermissionSigns();
            if (!packageSigns.isEmpty()) {
                for (String permissionSign : permissionSignList) {
                    if (!packageSigns.contains(permissionSign)) {
                        throw new ArtException("权限【" + permissionSign + "】超出租户套餐范围，保存失败！");
                    }
                }
            }
        }
        // 权限关系为系统级数据，写入不受租户过滤
        tenantSupport.systemScope(() -> {
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
                    cacheRefreshService.refreshAfterCommit(userPermissionCache);
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
                    cacheRefreshService.refreshAfterCommit(rolePermissionCache);
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
                    cacheRefreshService.refreshAfterCommit(deptPermissionCache);
                }
                default -> throw new ArtException("权限类型错误，请联系管理员！");
            }
            return null;
        });
        return true;
    }

    /**
     * 删除菜单权限
     *
     * @param menuPermissionSign 菜单权限标识
     */
    @Override
    public void deletePermissionByMenu(List<String> menuPermissionSign) {
        // 权限关系为系统级数据，写入不受租户过滤
        tenantSupport.systemScope(() -> {
            this.deptPermissionMapper.deleteByQuery(QueryWrapper.create().in(DeptPermission::getPermissionSign, menuPermissionSign));
            this.rolePermissionMapper.deleteByQuery(QueryWrapper.create().in(RolePermission::getPermissionSign, menuPermissionSign));
            this.userPermissionMapper.deleteByQuery(QueryWrapper.create().in(UserPermission::getPermissionSign, menuPermissionSign));
            // 权限数据变更后刷新相关缓存
            cacheRefreshService.refreshAfterCommit(userPermissionCache);
            cacheRefreshService.refreshAfterCommit(rolePermissionCache);
            cacheRefreshService.refreshAfterCommit(deptPermissionCache);
            return null;
        });
    }
}
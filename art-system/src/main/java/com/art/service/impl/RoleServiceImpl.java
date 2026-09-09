package com.art.service.impl;

import com.art.auth.cache.RoleCache;
import com.art.auth.cache.RolePermissionCache;
import com.art.auth.cache.UserRoleCache;
import com.art.cache.support.CacheRefreshService;
import com.art.common.SelectVO;
import com.art.common.UserRole;
import com.art.domain.Role;
import com.art.domain.RolePermission;
import com.art.domain.vo.RoleVO;
import com.art.exception.ArtException;
import com.art.mapper.RolePermissionMapper;
import com.art.mapper.UserRoleMapper;
import com.art.tenant.TenantSupport;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.art.mapper.RoleMapper;
import com.art.service.RoleService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 角色服务实现类。
 *
 * <p>角色为租户级数据：多租户开启后，角色表按当前生效租户自动过滤，
 * 各租户拥有各自独立的角色池，互不可见、互不影响。</p>
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    /**
     * 用户角色关系Mapper（关系表无租户字段，写入需自行控制作用域）
     */
    private final UserRoleMapper userRoleMapper;

    /**
     * 角色功能权限Mapper（关系表无租户字段，写入需自行控制作用域）
     */
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 缓存刷新服务
     */
    private final CacheRefreshService cacheRefreshService;

    /**
     * 角色缓存
     */
    private final RoleCache roleCache;

    /**
     * 用户角色关系缓存
     */
    private final UserRoleCache userRoleCache;

    /**
     * 角色功能权限缓存
     */
    private final RolePermissionCache rolePermissionCache;

    /**
     * 构造函数
     *
     * @param userRoleMapper      用户角色关系Mapper
     * @param rolePermissionMapper 角色功能权限Mapper
     * @param tenantSupport       多租户支持
     * @param cacheRefreshService 缓存刷新服务
     * @param roleCache           角色缓存
     * @param userRoleCache       用户角色关系缓存
     * @param rolePermissionCache 角色功能权限缓存
     */
    public RoleServiceImpl(UserRoleMapper userRoleMapper, RolePermissionMapper rolePermissionMapper,
                           TenantSupport tenantSupport, CacheRefreshService cacheRefreshService,
                           RoleCache roleCache, UserRoleCache userRoleCache, RolePermissionCache rolePermissionCache) {
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.tenantSupport = tenantSupport;
        this.cacheRefreshService = cacheRefreshService;
        this.roleCache = roleCache;
        this.userRoleCache = userRoleCache;
        this.rolePermissionCache = rolePermissionCache;
    }

    /**
     * 根据ID查询角色信息（自动限定当前租户）
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @Override
    public Role selectById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页查询角色信息（自动限定当前租户）
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 角色信息
     */
    @Override
    public Page<RoleVO> queryPage(Page<RoleVO> page, RoleVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        return this.getMapper().paginateAs(page, wrapper, RoleVO.class);
    }

    /**
     * 查询所有角色信息（自动限定当前租户）
     *
     * @return 角色信息
     */
    @Override
    public List<Role> selectList() {
        return this.list();
    }

    /**
     * 添加角色信息（自动归属当前租户）
     *
     * @param vo 角色信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(RoleVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Role entity = ConvertUtil.convert(vo, Role.class);
        boolean result = this.save(entity);
        if (result) {
            cacheRefreshService.refreshAfterCommit(roleCache);
        }
        return result;
    }

    /**
     * 编辑角色信息（自动限定当前租户）
     *
     * @param vo 角色信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(RoleVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Role entity = ConvertUtil.convert(vo, Role.class);
        boolean result = this.updateById(entity);
        if (result) {
            cacheRefreshService.refreshAfterCommit(roleCache);
        }
        return result;
    }

    /**
     * 删除角色信息（自动限定当前租户）
     *
     * <p>角色删除后必须同步清理用户角色关系与角色权限关系，否则被删角色的
     * 权限仍会被授权缓存计入，导致"删角色不收回权限"。</p>
     *
     * @param idList 角色ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return false;
        }
        List<Long> distinctIds = idList.stream().filter(Objects::nonNull).distinct().toList();
        if (distinctIds.isEmpty()) {
            return false;
        }
        // 先校验角色归属当前生效租户，避免按ID误删他租户的关系数据
        long count = this.count(QueryWrapper.create().in(Role::getId, distinctIds));
        if (count != distinctIds.size()) {
            throw new ArtException("包含非本租户的角色，操作失败！");
        }
        // 关系表为系统级数据（无租户字段），删除不受租户过滤
        tenantSupport.systemScope(() -> {
            this.userRoleMapper.deleteByQuery(QueryWrapper.create().in(UserRole::getRoleId, distinctIds));
            this.rolePermissionMapper.deleteByQuery(QueryWrapper.create().in(RolePermission::getRoleId, distinctIds));
            return null;
        });
        boolean result = this.removeByIds(distinctIds);
        if (result) {
            cacheRefreshService.refreshAfterCommit(roleCache);
            cacheRefreshService.refreshAfterCommit(userRoleCache);
            cacheRefreshService.refreshAfterCommit(rolePermissionCache);
        }
        return result;
    }

    /**
     * 角色树形下拉列表（自动限定当前租户）
     *
     * @return 角色树形下拉列表
     */
    @Override
    public List<SelectVO> select() {
        List<Role> roleList = this.list(QueryWrapper.create().eq(Role::getEnableFlag, 1));
        return roleList.stream().map(role -> {
            SelectVO selectVO = new SelectVO();
            selectVO.setLabel(role.getRoleName());
            selectVO.setValue(role.getId());
            return selectVO;
        }).toList();
    }

}
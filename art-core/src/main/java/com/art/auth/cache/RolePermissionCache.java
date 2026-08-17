package com.art.auth.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.domain.RolePermission;
import com.art.mapper.RolePermissionMapper;
import com.art.utils.SecurityUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色功能权限二级缓存实现（认证授权基础设施缓存，归属 art-core）
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class RolePermissionCache extends ArtCache<List<RolePermission>> {

    /**
     * 角色权限Mapper
     */
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate        Redis客户端
     * @param properties           缓存配置
     * @param rolePermissionMapper 角色权限Mapper
     */
    public RolePermissionCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, RolePermissionMapper rolePermissionMapper) {
        super(redisTemplate, properties);
        this.rolePermissionMapper = rolePermissionMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    public String cacheName() {
        return "角色功能权限";
    }

    /**
     * 获取缓存Key
     *
     * @return 缓存Key
     */
    @Override
    public String redisKey() {
        return "menuPermission:role";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<RolePermission> loadFromDb() {
        return this.rolePermissionMapper.selectAll();
    }

    /**
     * 获取当前角色权限
     *
     * @return 当前角色权限
     */
    public List<RolePermission> getByCurrentRole() {
        List<Long> roleIds = SecurityUtil.getRoleId();
        return get().stream()
                .filter(rolePermission -> roleIds.contains(rolePermission.getRoleId()))
                .toList();
    }

    /**
     * 根据角色ID获取角色权限
     *
     * @param roleId 角色ID
     * @return 角色权限
     */
    public List<RolePermission> getByRoleId(Long roleId) {
        return get().stream()
                .filter(rolePermission -> rolePermission.getRoleId().equals(roleId))
                .toList();
    }
}

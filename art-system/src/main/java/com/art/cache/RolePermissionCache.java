package com.art.cache;

import com.art.ArtCache;
import com.art.domain.RolePermission;
import com.art.mapper.RolePermissionMapper;
import com.art.utils.SecurityUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色权限缓存
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class RolePermissionCache extends ArtCache<String, List<RolePermission>> {

    /**
     * 角色权限Mapper
     */
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate        Redis客户端
     * @param rolePermissionMapper 角色权限Mapper
     */
    public RolePermissionCache(RedisTemplate<String, Object> redisTemplate, RolePermissionMapper rolePermissionMapper) {
        super(redisTemplate);
        this.rolePermissionMapper = rolePermissionMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    protected String getCacheName() {
        return "角色功能权限";
    }

    /**
     * 获取RedisKey
     *
     * @return RedisKey
     */
    @Override
    protected String getRedisKey() {
        return "menuPermission:role";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<RolePermission> getCacheData() {
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
}

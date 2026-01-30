package com.art.cache;

import com.art.ArtCache;
import com.art.domain.UserPermission;
import com.art.mapper.UserPermissionMapper;
import com.art.utils.SecurityUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户功能权限缓存
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class UserPermissionCache extends ArtCache<String, List<UserPermission>> {
    /**
     * 用户权限Mapper
     */
    private final UserPermissionMapper userPermissionMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate        Redis客户端
     * @param userPermissionMapper 用户权限Mapper
     */
    public UserPermissionCache(RedisTemplate<String, Object> redisTemplate, UserPermissionMapper userPermissionMapper) {
        super(redisTemplate);
        this.userPermissionMapper = userPermissionMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    protected String getCacheName() {
        return "用户功能权限";
    }

    /**
     * 获取RedisKey
     *
     * @return RedisKey
     */
    @Override
    protected String getRedisKey() {
        return "menuPermission:user";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<UserPermission> getCacheData() {
        return this.userPermissionMapper.selectAll();
    }

    /**
     * 获取当前用户权限
     *
     * @return 当前用户权限
     */
    public List<UserPermission> getByCurrentUser() {
        Long userId = SecurityUtil.getUserId();
        return get().stream()
                .filter(userPermission -> userPermission.getUserId().equals(userId))
                .toList();
    }
}

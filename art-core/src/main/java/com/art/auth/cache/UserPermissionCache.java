package com.art.auth.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.domain.UserPermission;
import com.art.mapper.UserPermissionMapper;
import com.art.utils.SecurityUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户功能权限二级缓存实现（认证授权基础设施缓存，归属 art-core）
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class UserPermissionCache extends ArtCache<List<UserPermission>> {
    /**
     * 用户权限Mapper
     */
    private final UserPermissionMapper userPermissionMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate        Redis客户端
     * @param properties           缓存配置
     * @param userPermissionMapper 用户权限Mapper
     */
    public UserPermissionCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, UserPermissionMapper userPermissionMapper) {
        super(redisTemplate, properties);
        this.userPermissionMapper = userPermissionMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    public String cacheName() {
        return "用户功能权限";
    }

    /**
     * 获取缓存Key
     *
     * @return 缓存Key
     */
    @Override
    public String redisKey() {
        return "menuPermission:user";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<UserPermission> loadFromDb() {
        return this.userPermissionMapper.selectAll();
    }

    /**
     * 获取当前用户权限
     *
     * @return 当前用户权限
     */
    public List<UserPermission> getByCurrentUser() {
        Long userId = SecurityUtil.getUserId();
        return this.getByUserId(userId);
    }

    /**
     * 根据用户ID获取用户权限
     *
     * @param userId 用户ID
     * @return 用户权限
     */
    public List<UserPermission> getByUserId(Long userId) {
        return get().stream()
                .filter(userPermission -> userPermission.getUserId().equals(userId))
                .toList();
    }
}

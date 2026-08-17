package com.art.auth.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.common.UserRole;
import com.art.mapper.UserRoleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户角色关系二级缓存实现（认证授权基础设施缓存，归属 art-core）
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class UserRoleCache extends ArtCache<List<UserRole>> {

    /**
     * 用户角色Mapper
     */
    private final UserRoleMapper userRoleMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate  Redis客户端
     * @param properties     缓存配置
     * @param userRoleMapper 用户角色Mapper
     */
    public UserRoleCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, UserRoleMapper userRoleMapper) {
        super(redisTemplate, properties);
        this.userRoleMapper = userRoleMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    public String cacheName() {
        return "用户角色关系";
    }

    /**
     * 获取Redis Key
     *
     * @return Redis Key
     */
    @Override
    public String redisKey() {
        return "userRole";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<UserRole> loadFromDb() {
        return this.userRoleMapper.selectListByQuery(QueryWrapper.create());
    }

    /**
     * 根据用户ID获取角色ID
     *
     * @param userId 用户ID
     * @return 角色ID
     */
    public List<Long> getRoleByUserId(Long userId) {
        List<UserRole> userRoles = get();
        return userRoles.stream()
                .filter(userRole -> userRole.getUserId().equals(userId))
                .map(UserRole::getRoleId).toList();
    }
}

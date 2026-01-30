package com.art.cache;

import com.art.ArtCache;
import com.art.common.UserRole;
import com.art.mapper.UserRoleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户角色缓存
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class UserRoleCache extends ArtCache<String, List<UserRole>> {

    private final UserRoleMapper userRoleMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate  Redis客户端
     * @param userRoleMapper 用户角色Mapper
     */
    public UserRoleCache(RedisTemplate<String, Object> redisTemplate, UserRoleMapper userRoleMapper) {
        super(redisTemplate);
        this.userRoleMapper = userRoleMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    protected String getCacheName() {
        return "用户角色关系";
    }

    /**
     * 获取Redis Key
     *
     * @return Redis Key
     */
    @Override
    protected String getRedisKey() {
        return "userRole";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<UserRole> getCacheData() {
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

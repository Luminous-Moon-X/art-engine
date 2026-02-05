package com.art.cache;

import com.art.ArtCache;
import com.art.domain.Role;
import com.art.mapper.RoleMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleCache extends ArtCache<String, List<Role>> {

    /**
     * 角色Mapper
     */
    private final RoleMapper roleMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param roleMapper    角色Mapper
     */
    public RoleCache(RedisTemplate<String, Object> redisTemplate, RoleMapper roleMapper) {
        super(redisTemplate);
        this.roleMapper = roleMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    protected String getCacheName() {
        return "角色数据";
    }

    /**
     * 获取Redis Key
     *
     * @return Redis Key
     */
    @Override
    protected String getRedisKey() {
        return "role";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<Role> getCacheData() {
        return this.roleMapper.selectListByQuery(QueryWrapper.create().eq(Role::getEnableFlag, true));
    }

    /**
     * 根据角色ID获取角色标识
     *
     * @param roleIds 角色ID
     * @return 角色标识
     */
    public List<String> getRoleCodeByIds(List<Long> roleIds) {
        List<Role> allRoles = this.get();
        return allRoles.stream()
                .filter(role -> roleIds.contains(role.getId()))
                .map(Role::getRoleCode)
                .toList();
    }
}

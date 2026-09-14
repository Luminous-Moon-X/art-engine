package com.art.auth.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.domain.Role;
import com.art.mapper.RoleMapper;
import com.art.tenant.TenantSupport;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 角色数据二级缓存实现（认证授权基础设施缓存，归属 art-core）
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class RoleCache extends ArtCache<List<Role>> {

    /**
     * 角色Mapper
     */
    private final RoleMapper roleMapper;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param properties    缓存配置
     * @param roleMapper    角色Mapper
     * @param tenantSupport 多租户支持
     */
    public RoleCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, RoleMapper roleMapper,
                     TenantSupport tenantSupport) {
        super(redisTemplate, properties);
        this.roleMapper = roleMapper;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    public String cacheName() {
        return "角色数据";
    }

    /**
     * 获取Redis Key
     *
     * @return Redis Key
     */
    @Override
    public String redisKey() {
        return "role";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<Role> loadFromDb() {
        // 角色为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() ->
                this.roleMapper.selectListByQuery(QueryWrapper.create().eq(Role::getEnableFlag, 1)));
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

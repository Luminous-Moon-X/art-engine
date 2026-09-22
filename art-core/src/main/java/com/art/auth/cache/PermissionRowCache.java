package com.art.auth.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.context.DataAuthContextHolder;
import com.art.domain.PermissionRow;
import com.art.mapper.PermissionRowMapper;
import com.art.tenant.TenantSupport;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 数据行权限缓存类
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Component("permissionRowCache")
public class PermissionRowCache extends ArtCache<Map<Long, List<PermissionRow>>> {
    /**
     * 权限行Mapper
     */
    private final PermissionRowMapper permissionRowMapper;
    /**
     * 租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param redisTemplate       Redis客户端
     * @param properties          缓存配置
     * @param permissionRowMapper 权限行Mapper
     * @param tenantSupport       租户支持
     */
    public PermissionRowCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties,
                              PermissionRowMapper permissionRowMapper, TenantSupport tenantSupport) {
        super(redisTemplate, properties);
        this.permissionRowMapper = permissionRowMapper;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 从数据库加载数据
     *
     * @return 缓存数据
     */
    @Override
    protected Map<Long, List<PermissionRow>> loadFromDb() {
        // 加载权限规则自身时必须忽略数据行权限，否则方言会再次读取本缓存（此时缓存尚未写入）造成无限递归
        return DataAuthContextHolder.ignoreScope(() -> {
            AtomicReference<Map<Long, List<PermissionRow>>> tenantPermissionRow = new AtomicReference<>(Map.of());
            tenantSupport.systemScope(() -> {
                List<PermissionRow> allPermissionRows = permissionRowMapper.selectAll();
                tenantPermissionRow.set(allPermissionRows.stream()
                        .collect(Collectors.groupingBy(PermissionRow::getTenantId)));
            });
            return tenantPermissionRow.get();
        });
    }

    /**
     * 根据租户ID获取对应租户的数据行权限规则List
     *
     * @param tenantId 租户ID
     * @return 数据行权限规则List
     */
    public List<PermissionRow> getByTenantId(Long tenantId) {
        Map<Long, List<PermissionRow>> allTenantPermissionRows = this.get();
        if (!allTenantPermissionRows.containsKey(tenantId)) {
            return Collections.emptyList();
        }
        return allTenantPermissionRows.get(tenantId);
    }

    /**
     * 缓存名称
     */
    @Override
    public String cacheName() {
        return "数据行权限";
    }

    /**
     * 缓存键
     *
     * @return 缓存键
     */
    @Override
    public String redisKey() {
        return "permissionRow";
    }
}

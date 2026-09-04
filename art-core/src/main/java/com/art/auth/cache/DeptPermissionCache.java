package com.art.auth.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.domain.DeptPermission;
import com.art.mapper.DeptPermissionMapper;
import com.art.tenant.TenantSupport;
import com.art.utils.SecurityUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 部门功能权限二级缓存实现（认证授权基础设施缓存，归属 art-core）
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class DeptPermissionCache extends ArtCache<List<DeptPermission>> {
    /**
     * 部门权限Mapper
     */
    private final DeptPermissionMapper deptPermissionMapper;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param redisTemplate        Redis客户端
     * @param properties           缓存配置
     * @param deptPermissionMapper 部门权限Mapper
     * @param tenantSupport        多租户支持
     */
    public DeptPermissionCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, DeptPermissionMapper deptPermissionMapper,
                               TenantSupport tenantSupport) {
        super(redisTemplate, properties);
        this.deptPermissionMapper = deptPermissionMapper;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    public String cacheName() {
        return "部门功能权限";
    }

    /**
     * 获取缓存Key
     *
     * @return 缓存Key
     */
    @Override
    public String redisKey() {
        return "menuPermission:dept";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<DeptPermission> loadFromDb() {
        // 部门功能权限为系统级数据，不受租户过滤
        return tenantSupport.systemScope(deptPermissionMapper::selectAll);
    }

    /**
     * 获取当前部门权限
     *
     * @return 当前部门权限
     */
    public List<DeptPermission> getByCurrentDept() {
        Long deptId = SecurityUtil.getDeptId();
        return this.getByDeptId(deptId);
    }

    /**
     * 根据部门ID获取部门权限
     *
     * @param deptId 部门ID
     * @return 部门权限
     */
    public List<DeptPermission> getByDeptId(Long deptId) {
        return get().stream()
                .filter(deptPermission -> deptId.equals(deptPermission.getDeptId()))
                .toList();
    }
}

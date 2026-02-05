package com.art.cache;

import com.art.ArtCache;
import com.art.domain.DeptPermission;
import com.art.mapper.DeptPermissionMapper;
import com.art.utils.SecurityUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 部门功能权限缓存
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class DeptPermissionCache extends ArtCache<String, List<DeptPermission>> {
    /**
     * 部门权限Mapper
     */
    private final DeptPermissionMapper deptPermissionMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate        Redis客户端
     * @param deptPermissionMapper 部门权限Mapper
     */
    public DeptPermissionCache(RedisTemplate<String, Object> redisTemplate, DeptPermissionMapper deptPermissionMapper) {
        super(redisTemplate);
        this.deptPermissionMapper = deptPermissionMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    protected String getCacheName() {
        return "部门功能权限";
    }

    /**
     * 获取RedisKey
     *
     * @return RedisKey
     */
    @Override
    protected String getRedisKey() {
        return "menuPermission:dept";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<DeptPermission> getCacheData() {
        return deptPermissionMapper.selectAll();
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

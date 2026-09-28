package com.art.auth.cache;

import com.art.cache.support.ArtCacheProperties;
import com.art.context.DataAuthContextHolder;
import com.art.domain.PermissionRow;
import com.art.mapper.PermissionRowMapper;
import com.art.properties.TenantProperties;
import com.art.tenant.TenantSupport;
import com.mybatisflex.core.tenant.TenantManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * 数据行权限缓存测试
 *
 * <p>该缓存是数据行权限的唯一规则来源，加载逻辑必须：</p>
 * <ul>
 *     <li>按租户分组，且加载期间忽略数据行权限（否则方言读取本缓存会造成无限递归）并跳过租户条件；</li>
 *     <li>对缺少租户ID的脏数据具备容错能力（分组时空 key 会抛 NPE，将导致所有经由方言的查询整体失败）；</li>
 *     <li>未命中租户时返回空集合而不是抛异常。</li>
 * </ul>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
class PermissionRowCacheTest {

    /**
     * 构造被测试的缓存<br/>
     * <p>{@link TenantSupport} 使用真实实例（仅操作 MyBatis-Flex 的 ThreadLocal 开关），
     * Redis 以桩替代，避免依赖真实中间件。</p>
     *
     * @param permissionRowMapper 权限行 Mapper
     * @return 数据行权限缓存
     */
    @SuppressWarnings("unchecked")
    private PermissionRowCache newCache(PermissionRowMapper permissionRowMapper) {
        return new PermissionRowCache(mock(RedisTemplate.class), new ArtCacheProperties(),
                permissionRowMapper, new TenantSupport(new TenantProperties()));
    }

    /**
     * 加载结果按租户ID分组
     */
    @Test
    @DisplayName("加载规则：按租户分组")
    void shouldGroupRulesByTenantId() {
        PermissionRowMapper mapper = mock(PermissionRowMapper.class);
        when(mapper.selectAll()).thenReturn(List.of(
                this.rule(1L), this.rule(1L), this.rule(2L)));

        Map<Long, List<PermissionRow>> result = this.newCache(mapper).loadFromDb();

        assertThat(result).containsOnlyKeys(1L, 2L);
        assertThat(result.get(1L)).hasSize(2);
        assertThat(result.get(2L)).hasSize(1);
    }

    /**
     * 加载期间必须忽略数据行权限与租户条件，加载完成后恢复原状态
     */
    @Test
    @DisplayName("加载规则：忽略数据行权限与租户条件并恢复现场")
    void shouldIgnoreDataAuthAndTenantConditionWhileLoading() {
        PermissionRowMapper mapper = mock(PermissionRowMapper.class);
        // 记录真实执行 SQL 时的上下文开关状态
        List<Boolean> ignoreDataAuth = new ArrayList<>();
        List<Boolean> ignoreTenant = new ArrayList<>();
        when(mapper.selectAll()).thenAnswer(invocation -> {
            ignoreDataAuth.add(DataAuthContextHolder.ignore());
            ignoreTenant.add(TenantManager.isIgnoreTenantCondition());
            return List.of(this.rule(1L));
        });

        assertThat(DataAuthContextHolder.ignore()).isFalse();
        this.newCache(mapper).loadFromDb();

        assertThat(ignoreDataAuth).containsExactly(Boolean.TRUE);
        assertThat(ignoreTenant).containsExactly(Boolean.TRUE);
        // 现场必须恢复，否则同一线程后续查询会静默放开数据权限
        assertThat(DataAuthContextHolder.ignore()).isFalse();
        assertThat(TenantManager.isIgnoreTenantCondition()).isFalse();
    }

    /**
     * 缺少租户ID的规则必须被忽略而不是让整个缓存加载失败<br/>
     * <p>回归保护：规则表 tenant_id 允许为空，若直接 {@code Collectors.groupingBy}
     * 会因 null key 抛 NPE，而方言构建 SQL 时会读取本缓存，最终导致所有查询失败。</p>
     */
    @Test
    @DisplayName("加载规则：租户ID为空的脏数据被忽略")
    void shouldIgnoreRuleWithoutTenantId() {
        PermissionRow orphan = this.rule(null);
        orphan.setId(999L);
        PermissionRowMapper mapper = mock(PermissionRowMapper.class);
        when(mapper.selectAll()).thenReturn(List.of(this.rule(1L), orphan));

        Map<Long, List<PermissionRow>> result = this.newCache(mapper).loadFromDb();

        assertThat(result).containsOnlyKeys(1L);
        assertThat(result.get(1L)).hasSize(1);
    }

    /**
     * 按租户取规则：命中返回规则列表，未命中返回空集合（不抛异常）
     */
    @Test
    @DisplayName("按租户取规则：未命中返回空集合")
    void shouldReturnEmptyListForUnknownTenant() {
        PermissionRowCache cache = spy(this.newCache(mock(PermissionRowMapper.class)));
        Map<Long, List<PermissionRow>> data = new HashMap<>();
        data.put(1L, List.of(this.rule(1L)));
        doReturn(data).when(cache).get();

        assertThat(cache.getByTenantId(1L)).hasSize(1);
        assertThat(cache.getByTenantId(2L)).isEmpty();
        // 无租户上下文（如白名单请求）时同样返回空集合
        assertThat(cache.getByTenantId(null)).isEmpty();
    }

    /**
     * 缓存标识用于注册与失效广播，必须保持稳定
     */
    @Test
    @DisplayName("缓存标识：名称与Key稳定")
    void shouldExposeStableCacheIdentity() {
        PermissionRowCache cache = this.newCache(mock(PermissionRowMapper.class));
        assertThat(cache.redisKey()).isEqualTo("permissionRow");
        assertThat(cache.cacheName()).isEqualTo("数据行权限");
    }

    /**
     * 构建数据行权限规则
     *
     * @param tenantId 租户ID
     * @return 数据行权限规则
     */
    private PermissionRow rule(Long tenantId) {
        PermissionRow rule = new PermissionRow();
        rule.setTenantId(tenantId);
        rule.setEnableFlag(Boolean.TRUE);
        return rule;
    }
}

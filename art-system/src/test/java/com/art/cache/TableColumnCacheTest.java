package com.art.cache;

import com.art.cache.support.ArtCacheProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * 数据库表字段元数据缓存测试
 *
 * <p>该缓存支撑数据行权限对授权客体的字段判定：授权客体缺少授权范围所需字段时，
 * 方言会跳过该规则的数据权限处理，因此字段判定必须准确且忽略大小写。</p>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
class TableColumnCacheTest {

    /**
     * 构造被测试的缓存（JdbcTemplate 以桩替代，避免依赖真实数据库）
     */
    @SuppressWarnings("unchecked")
    private TableColumnCache newCache(JdbcTemplate jdbcTemplate) {
        return new TableColumnCache(mock(RedisTemplate.class), new ArtCacheProperties(), jdbcTemplate);
    }

    /**
     * 加载结果按「表名→字段集合」归集，且表名与字段名统一转为小写
     */
    @Test
    @DisplayName("加载元数据：按表归集并统一小写")
    void shouldLoadColumnsGroupedByLowercaseTableName() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForList(anyString())).thenReturn(List.of(
                Map.of("table_name", "P_SYS_User", "column_name", "ID"),
                Map.of("table_name", "p_sys_user", "column_name", "DEPT_ID"),
                Map.of("table_name", "p_sys_dict", "column_name", "dict_code")));

        Map<String, Set<String>> tableColumns = this.newCache(jdbcTemplate).loadFromDb();

        assertThat(tableColumns).containsOnlyKeys("p_sys_user", "p_sys_dict");
        assertThat(tableColumns.get("p_sys_user")).containsExactlyInAnyOrder("id", "dept_id");
        assertThat(tableColumns.get("p_sys_dict")).containsExactly("dict_code");
    }

    /**
     * 字段判定忽略大小写，且能正确区分「表存在但无该字段」
     */
    @Test
    @DisplayName("字段判定：忽略大小写并区分无该字段")
    void shouldMatchColumnIgnoringCase() {
        TableColumnCache cache = this.cacheWithColumns(Map.of(
                "p_sys_user", Set.of("id", "dept_id", "create_id"),
                "p_sys_dict", Set.of("id", "dict_code")));

        assertThat(cache.hasColumn("p_sys_user", "dept_id")).isTrue();
        assertThat(cache.hasColumn("P_SYS_USER", "DEPT_ID")).isTrue();
        assertThat(cache.hasColumn(" p_sys_user ", " dept_id ")).isTrue();
        // 授权客体存在但缺少该字段：必须返回 false，方言据此跳过该规则
        assertThat(cache.hasColumn("p_sys_dict", "dept_id")).isFalse();
        assertThat(cache.hasColumn("p_sys_dict", "create_id")).isFalse();
        // 表本身不存在
        assertThat(cache.hasColumn("not_exists_table", "dept_id")).isFalse();
    }

    /**
     * 表名或字段名为空时直接判定为不存在，避免空指针
     */
    @Test
    @DisplayName("字段判定：空入参返回 false")
    void shouldReturnFalseForBlankArguments() {
        TableColumnCache cache = this.cacheWithColumns(Map.of("p_sys_user", Set.of("dept_id")));

        assertThat(cache.hasColumn(null, "dept_id")).isFalse();
        assertThat(cache.hasColumn("", "dept_id")).isFalse();
        assertThat(cache.hasColumn("  ", "dept_id")).isFalse();
        assertThat(cache.hasColumn("p_sys_user", null)).isFalse();
        assertThat(cache.hasColumn("p_sys_user", "")).isFalse();
    }

    /**
     * 构造一个已注入元数据的缓存实例（绕过 Redis，直接桩定 get()）
     */
    private TableColumnCache cacheWithColumns(Map<String, Set<String>> tableColumns) {
        TableColumnCache cache = spy(this.newCache(mock(JdbcTemplate.class)));
        doReturn(new HashMap<>(tableColumns)).when(cache).get();
        return cache;
    }
}

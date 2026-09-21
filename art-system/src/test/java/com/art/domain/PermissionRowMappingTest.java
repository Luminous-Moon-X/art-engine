package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 数据行权限实体映射测试
 *
 * <p>回归保护：{@link PermissionRow} 的列映射必须与 {@code p_sys_permission_row} 表结构一致，
 * 否则会在运行期报错：</p>
 * <ul>
 *     <li>{@code delete_flag} 必须是 boolean，映射为 {@link BaseEntity} 的 Boolean；</li>
 *     <li>{@code enable_flag} 为 boolean，映射为 Boolean；</li>
 *     <li>租户列必须保留，保证多租户自动过滤生效。</li>
 * </ul>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
class PermissionRowMappingTest {

    /**
     * 表信息
     */
    private final TableInfo tableInfo = TableInfoFactory.ofEntityClass(PermissionRow.class);

    /**
     * delete_flag 应正常参与映射，且类型为 Boolean（与库中 boolean 对应）
     */
    @Test
    void deleteFlagShouldMapAsBoolean() {
        List<String> columns = Arrays.asList(this.tableInfo.getColumns());
        assertTrue(columns.contains("delete_flag"), "delete_flag 应参与映射，实际列：" + columns);

        ColumnInfo columnInfo = this.columnInfo("delete_flag");
        assertEquals(Boolean.class, columnInfo.getPropertyType());
        assertFalse(columnInfo.isIgnore());
    }

    /**
     * 启用标识为 Boolean，与数据库 boolean 列类型一致
     */
    @Test
    void enableFlagShouldMapAsBoolean() {
        ColumnInfo columnInfo = this.columnInfo("enable_flag");
        assertEquals(Boolean.class, columnInfo.getPropertyType());
        assertFalse(columnInfo.isIgnore());
    }

    /**
     * 租户列必须保留，保证多租户自动过滤生效
     */
    @Test
    void tenantColumnShouldBeMapped() {
        assertEquals("tenant_id", this.tableInfo.getTenantIdColumn());
        assertTrue(Arrays.asList(this.tableInfo.getColumns()).contains("tenant_id"));
    }

    /**
     * 数据权限业务字段应全部参与映射
     */
    @Test
    void businessColumnsShouldBeMapped() {
        List<String> columns = Arrays.asList(this.tableInfo.getColumns());
        List<String> expected = List.of("subject_type", "permission_subject", "permission_object",
                "permission_scope", "custom_dept_scope", "column_condition", "column_relation", "column_value");
        expected.forEach(column -> assertTrue(columns.contains(column), "缺少列：" + column));
    }

    /**
     * 获取指定列的列信息
     *
     * @param columnName 列名
     * @return 列信息
     */
    private ColumnInfo columnInfo(String columnName) {
        return this.tableInfo.getColumnInfoList().stream()
                .filter(info -> columnName.equals(info.getColumn()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到列：" + columnName));
    }
}

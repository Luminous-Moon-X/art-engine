package com.art.auth;

import com.art.auth.cache.PermissionRowCache;
import com.art.context.SecurityContextHolder;
import com.art.domain.PermissionRow;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.query.CPI;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 数据行权限条件拼接测试
 *
 * <p>覆盖 {@link DataAuthDialect#prepareAuth(QueryWrapper, com.mybatisflex.core.dialect.OperateType)}
 * 的规则命中、授权范围拼接、管理员豁免与幂等性，并固化以下实现边界：</p>
 * <ul>
 *     <li>非 SELECT 操作不做数据行权限处理；</li>
 *     <li>原始 SQL 路径与 {@code TableInfo} 路径（{@code getById} / {@code listByIds}）未接入数据行权限；</li>
 *     <li>JOIN 关联表不参与授权客体匹配；</li>
 *     <li>命中规则因授权客体缺字段被全部跳过时不做任何限制。</li>
 * </ul>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
class DataAuthDialectTest {

    /**
     * 当前登录人ID
     */
    private static final Long USER_ID = 1001L;
    /**
     * 当前登录人部门ID
     */
    private static final Long DEPT_ID = 2002L;
    /**
     * 当前登录人角色ID
     */
    private static final Long ROLE_ID = 3003L;
    /**
     * 当前租户ID
     */
    private static final Long TENANT_ID = 1L;
    /**
     * 目标表
     */
    private static final String TABLE_NAME = "p_sys_user";

    /**
     * 数据行权限缓存
     */
    private PermissionRowCache permissionRowCache;
    /**
     * 授权客体字段元数据提供者
     */
    private DataAuthColumnProvider columnProvider;
    /**
     * 授权客体字段元数据提供者的延迟提供者
     */
    private ObjectProvider<DataAuthColumnProvider> columnProviderHolder;
    /**
     * 被测试的方言
     */
    private DataAuthDialect dialect;

    /**
     * 归一化 SQL 空白字符<br/>
     * <p>SQL 拼接过程中会产生多余空格（如 {@code WHERE} 与条件之间的间隔），
     * 这些空白不影响语义，断言时统一归一化，聚焦条件本身。</p>
     *
     * @param sql SQL 语句
     * @return 归一化后的 SQL
     */
    private static String normalize(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * 初始化登录上下文与方言
     */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        this.permissionRowCache = mock(PermissionRowCache.class);
        ObjectProvider<PermissionRowCache> provider = mock(ObjectProvider.class);
        when(provider.getObject()).thenReturn(this.permissionRowCache);

        // 默认所有授权客体都包含授权范围所需字段
        this.columnProvider = mock(DataAuthColumnProvider.class);
        when(this.columnProvider.hasColumn(anyString(), anyString())).thenReturn(true);
        this.columnProviderHolder = mock(ObjectProvider.class);
        when(this.columnProviderHolder.getIfUnique()).thenReturn(this.columnProvider);

        this.dialect = new DataAuthDialect(provider, this.columnProviderHolder);
        this.resetContext();
    }

    /**
     * 清理登录上下文
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    /**
     * 授权范围1-所属部门：拼接 dept_id 等于当前登录人部门
     */
    @Test
    @DisplayName("授权范围1-所属部门：dept_id = 当前部门")
    void shouldBuildDeptScopeCondition() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ?");
        assertThat(this.selectParams()).containsExactly(DEPT_ID);
    }

    /**
     * 授权范围2-所属部门及以下：拼接部门子树递归子查询
     */
    @Test
    @DisplayName("授权范围2-所属部门及以下：dept_id IN (递归子查询)")
    void shouldBuildDeptAndChildScopeCondition() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "2", null, null, null, null));
        String expected = "SELECT * FROM \"p_sys_user\" WHERE \"p_sys_user\".\"dept_id\" IN ("
                + "WITH RECURSIVE \"data_auth_dept_tree\" AS ("
                + "SELECT \"id\" FROM \"p_sys_dept\" WHERE \"id\" = ? AND \"delete_flag\" = 0 AND \"tenant_id\" = ? "
                + "UNION ALL "
                + "SELECT \"d\".\"id\" FROM \"p_sys_dept\" AS \"d\" INNER JOIN \"data_auth_dept_tree\" AS \"t\" "
                + "ON \"d\".\"parent_id\" = \"t\".\"id\" WHERE \"d\".\"delete_flag\" = 0 AND \"d\".\"tenant_id\" = ? "
                + ") SELECT \"id\" FROM \"data_auth_dept_tree\")";
        assertThat(normalize(this.selectSql())).isEqualTo(normalize(expected));
        // 非递归项使用当前部门，递归项两处租户条件复用当前租户
        assertThat(this.selectParams()).containsExactly(DEPT_ID, TENANT_ID, TENANT_ID);
    }

    /**
     * 授权范围2-所属部门及以下：查询表带别名时以别名限定字段
     */
    @Test
    @DisplayName("授权范围2-所属部门及以下：带表别名")
    void shouldQualifyColumnWithTableAlias() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "2", null, null, null, null));
        QueryWrapper wrapper = QueryWrapper.create().from(TABLE_NAME).as("u");
        assertThat(normalize(this.dialect.forSelectByQuery(wrapper)))
                .startsWith("SELECT * FROM \"p_sys_user\" AS \"u\" WHERE \"u\".\"dept_id\" IN (");
    }

    /**
     * 授权范围3-本人创建数据：拼接 create_id 等于当前登录人
     */
    @Test
    @DisplayName("授权范围3-本人创建数据：create_id = 当前登录人")
    void shouldBuildSelfScopeCondition() {
        this.mockRules(this.rule("user", String.valueOf(USER_ID), TABLE_NAME, "3", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"create_id\" = ?");
        assertThat(this.selectParams()).containsExactly(USER_ID);
    }

    /**
     * 授权范围4-自定义部门范围：拼接自定义部门集合
     */
    @Test
    @DisplayName("授权范围4-自定义部门范围：dept_id IN (自定义部门)")
    void shouldBuildCustomDeptScopeCondition() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "4", "11,22", null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" IN (?, ?)");
        assertThat(this.selectParams()).containsExactly(11L, 22L);
    }

    /**
     * 授权范围5-自定义字段：等于关系以字面量拼接（避免数值列类型不匹配）
     */
    @Test
    @DisplayName("授权范围5-自定义字段：等于关系")
    void shouldBuildCustomColumnEqualsCondition() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "amount", "eq", "20"));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"amount\" = E'20'");
        // 值为字面量，不占用绑定参数
        assertThat(this.selectParams()).isEmpty();
    }

    /**
     * 授权范围5-自定义字段：为空/不为空不拼接字段值
     */
    @Test
    @DisplayName("授权范围5-自定义字段：为空与不为空")
    void shouldBuildCustomColumnNullCondition() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "remark", "null", null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"remark\" IS NULL");

        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "remark", "not_null", null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"remark\" IS NOT NULL");
    }

    /**
     * 授权范围5-自定义字段：包含关系自动补通配符
     */
    @Test
    @DisplayName("授权范围5-自定义字段：包含关系")
    void shouldBuildCustomColumnLikeCondition() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "user_name", "like", "admin"));
        assertThat(normalize(this.selectSql()))
                .isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"user_name\" LIKE E'%admin%'");
    }

    /**
     * 授权范围5-自定义字段：字段值中的单引号必须被转义，杜绝 SQL 注入
     */
    @Test
    @DisplayName("授权范围5-自定义字段：字段值单引号被转义")
    void shouldEscapeColumnValue() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "user_name", "eq", "' OR 1=1 --"));
        assertThat(this.selectSql())
                .isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"user_name\" = E'\\' OR 1=1 --'");
        assertThat(this.selectParams()).isEmpty();
    }

    /**
     * 授权范围5-自定义字段：反斜杠同样被转义
     */
    @Test
    @DisplayName("授权范围5-自定义字段：字段值反斜杠被转义")
    void shouldEscapeColumnValueBackslash() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "user_name", "eq", "a\\b"));
        assertThat(this.selectSql())
                .isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"user_name\" = E'a\\\\b'");
    }

    /**
     * 授权范围5-自定义字段：非法列名（注入）直接拒绝
     */
    @Test
    @DisplayName("授权范围5-自定义字段：非法列名拒绝放行")
    void shouldDenyIllegalColumnName() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "1=1 OR id", "eq", "1"));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 授权范围5-自定义字段：需要字段值的关系未配置字段值时拒绝放行<br/>
     * <p>回归保护：包含关系会为字段值拼接通配符，必须在校验之后再进行拼接，
     * 否则空值会被拼成 {@code %null%} 而绕过校验。</p>
     */
    @Test
    @DisplayName("授权范围5-自定义字段：缺少字段值拒绝放行")
    void shouldDenyWhenColumnValueMissing() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "user_name", "like", null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");

        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "user_name", "eq", "  "));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 授权客体未命中当前查询表时不拼接条件
     */
    @Test
    @DisplayName("授权客体未命中：不拼接条件")
    void shouldSkipWhenPermissionObjectNotMatched() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), "p_sys_dept", "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 授权客体为空时对所有表生效
     */
    @Test
    @DisplayName("授权客体为空：对所有表生效")
    void shouldApplyWhenPermissionObjectBlank() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), "", "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ?");
    }

    /**
     * 规则未启用时不拼接条件
     */
    @Test
    @DisplayName("规则未启用：不拼接条件")
    void shouldSkipDisabledRule() {
        PermissionRow rule = this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null);
        rule.setEnableFlag(Boolean.FALSE);
        this.mockRules(rule);
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 授权主体未命中当前登录人时不拼接条件
     */
    @Test
    @DisplayName("授权主体未命中：不拼接条件")
    void shouldSkipWhenSubjectNotMatched() {
        this.mockRules(this.rule("role", "999999", TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 授权主体支持逗号分隔的多值
     */
    @Test
    @DisplayName("授权主体多值：任一命中即生效")
    void shouldMatchAnySubjectValue() {
        this.mockRules(this.rule("role", "999999," + ROLE_ID, TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ?");
    }

    /**
     * 授权主体类型为部门时同样可以命中
     */
    @Test
    @DisplayName("授权主体为部门：命中当前部门")
    void shouldMatchDeptSubject() {
        this.mockRules(this.rule("dept", String.valueOf(DEPT_ID), TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ?");
    }

    /**
     * 超级管理员与租户管理员豁免数据行权限
     */
    @Test
    @DisplayName("管理员豁免：超级管理员与租户管理员不过滤")
    void shouldSkipForAdmin() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));

        SecurityContextHolder.setUserType("superadmin");
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");

        SecurityContextHolder.setUserType("admin");
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 多条命中规则按并集（OR）合并
     */
    @Test
    @DisplayName("多规则：按并集合并")
    void shouldUnionMultipleRules() {
        this.mockRules(
                this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null),
                this.rule("user", String.valueOf(USER_ID), TABLE_NAME, "3", null, null, null, null));
        assertThat(normalize(this.selectSql()))
                .isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ? OR \"create_id\" = ?");
        assertThat(this.selectParams()).containsExactly(DEPT_ID, USER_ID);
    }

    /**
     * 已有查询条件时，权限条件以 AND 追加并加括号保证优先级
     */
    @Test
    @DisplayName("已有条件：AND 追加并加括号")
    void shouldAppendWithAndBrackets() {
        this.mockRules(
                this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null),
                this.rule("user", String.valueOf(USER_ID), TABLE_NAME, "3", null, null, null, null));
        QueryWrapper wrapper = QueryWrapper.create().from(TABLE_NAME).eq("user_status", "0");
        assertThat(normalize(this.dialect.forSelectByQuery(wrapper)))
                .isEqualTo("SELECT * FROM \"p_sys_user\" WHERE user_status = ? AND (\"dept_id\" = ? OR \"create_id\" = ?)");
        assertThat(CPI.getValueArray(wrapper)).containsExactly("0", DEPT_ID, USER_ID);
    }

    /**
     * 同一查询条件对象重复构建 SQL 时权限条件只拼接一次
     */
    @Test
    @DisplayName("幂等：重复构建 SQL 不重复拼接")
    void shouldNotDuplicateCondition() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        QueryWrapper wrapper = QueryWrapper.create().from(TABLE_NAME);
        String first = this.dialect.forSelectByQuery(wrapper);
        String second = this.dialect.forSelectByQuery(wrapper);
        assertThat(second).isEqualTo(first);
        assertThat(CPI.getValueArray(wrapper)).containsExactly(DEPT_ID);
    }

    /**
     * 克隆后的查询条件对象继承已拼接的权限条件与标记，不会漏加也不会重复
     */
    @Test
    @DisplayName("克隆体：继承权限条件且不重复拼接")
    void shouldKeepConditionOnClone() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        QueryWrapper wrapper = QueryWrapper.create().from(TABLE_NAME);
        String sql = this.dialect.forSelectByQuery(wrapper);
        QueryWrapper clone = wrapper.clone();
        assertThat(this.dialect.forSelectByQuery(clone)).isEqualTo(sql);
        assertThat(CPI.getValueArray(clone)).containsExactly(DEPT_ID);
    }

    /**
     * 规则已命中但登录人无归属部门时兜底拒绝，避免越权放行
     */
    @Test
    @DisplayName("登录人无部门：兜底拒绝")
    void shouldDenyWhenDeptIdMissing() {
        SecurityContextHolder.setDeptId(null);
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 登录人无归属部门时，授权范围4（自定义部门范围）不受影响
     */
    @Test
    @DisplayName("登录人无部门：自定义部门范围仍生效")
    void shouldKeepCustomDeptScopeWhenDeptIdMissing() {
        SecurityContextHolder.setDeptId(null);
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "4", "11", null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" IN (?)");
        assertThat(this.selectParams()).containsExactly(11L);
    }

    /**
     * 登录人无角色时不抛异常，且不匹配任何角色规则
     */
    @Test
    @DisplayName("登录人无角色：不抛异常且不匹配角色规则")
    void shouldNotFailWhenRoleIdsMissing() {
        SecurityContextHolder.setRoleIds(List.of());
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 当前租户无规则时不拼接条件
     */
    @Test
    @DisplayName("无规则：不拼接条件")
    void shouldSkipWhenNoRule() {
        this.mockRules();
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 授权客体缺少授权范围所需字段时跳过该规则（不做数据权限处理）
     * <p>回归保护：授权客体由管理员从全库表清单中选择，可能存在不含 dept_id / create_id 的表，
     * 若继续拼接条件会导致该表的所有查询报「字段不存在」。</p>
     */
    @Test
    @DisplayName("授权客体缺少所需字段：跳过该规则")
    void shouldSkipRuleWhenColumnMissing() {
        // 部门类授权范围依赖 dept_id
        when(this.columnProvider.hasColumn(TABLE_NAME, "dept_id")).thenReturn(false);
        for (String scope : List.of("1", "2", "4")) {
            this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, scope, "11", null, null, null));
            assertThat(normalize(this.selectSql()))
                    .as("授权范围[%s]应跳过", scope)
                    .isEqualTo("SELECT * FROM \"p_sys_user\"");
        }

        // 本人创建依赖 create_id
        when(this.columnProvider.hasColumn(TABLE_NAME, "create_id")).thenReturn(false);
        this.mockRules(this.rule("user", String.valueOf(USER_ID), TABLE_NAME, "3", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");

        // 自定义字段依赖规则配置的权限字段
        when(this.columnProvider.hasColumn(TABLE_NAME, "amount")).thenReturn(false);
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "amount", "eq", "20"));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 授权客体字段元数据不可用时不做判定，保持原有拼接行为（避免静默放开数据）
     */
    @Test
    @DisplayName("字段元数据不可用：保持拼接条件")
    void shouldKeepConditionWhenColumnMetadataUnavailable() {
        when(this.columnProviderHolder.getIfUnique()).thenReturn(null);
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ?");
    }

    /**
     * 子查询派生表取不到真实表名时不做字段判定，保持原有拼接行为
     */
    @Test
    @DisplayName("派生表：不做字段判定")
    void shouldKeepConditionForDerivedTable() {
        when(this.columnProvider.hasColumn(anyString(), anyString())).thenReturn(false);
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), "", "1", null, null, null, null));

        QueryWrapper subQuery = QueryWrapper.create().select("id", "dept_id").from(TABLE_NAME);
        QueryWrapper wrapper = QueryWrapper.create().from(subQuery).as("t");
        assertThat(normalize(this.dialect.forSelectByQuery(wrapper)))
                .contains("WHERE \"t\".\"dept_id\" = ?");
    }

    /**
     * 忽略数据行权限上下文（权限规则自身加载）时不拼接条件
     */
    @Test
    @DisplayName("忽略上下文：不拼接条件")
    void shouldSkipWhenIgnoreContext() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        String sql = com.art.context.DataAuthContextHolder.ignoreScope(this::selectSql);
        assertThat(normalize(sql)).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 非查询操作不做数据行权限处理，且不会消费掉权限条件
     */
    @Test
    @DisplayName("非查询操作：UPDATE 不拼接条件")
    void shouldSkipWhenOperateTypeIsNotSelect() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        QueryWrapper wrapper = QueryWrapper.create().from(TABLE_NAME);
        this.dialect.prepareAuth(wrapper, OperateType.UPDATE);
        assertThat(normalize(this.dialect.buildSelectSql(wrapper))).isEqualTo("SELECT * FROM \"p_sys_user\"");
        assertThat(CPI.getValueArray(wrapper)).isEmpty();
        // 未消费权限条件：随后按查询方式构建时仍会正常拼接
        assertThat(normalize(this.dialect.forSelectByQuery(wrapper)))
                .isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ?");
    }

    /**
     * 未指定查询表（无 FROM）时无法判断授权客体，不拼接条件
     */
    @Test
    @DisplayName("无查询表：不拼接条件")
    void shouldSkipWhenQueryTableMissing() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        QueryWrapper wrapper = QueryWrapper.create().select("1");
        assertThat(this.dialect.forSelectByQuery(wrapper)).doesNotContain("dept_id");
        assertThat(CPI.getValueArray(wrapper)).isEmpty();
    }

    /**
     * 直接拼接 SQL 文本的查询路径（{@code forSelectOneById} 等）当前不接入数据行权限<br/>
     * <p>守护用例：数据行权限仅覆盖 {@link QueryWrapper} 查询路径，若后续接入该路径，
     * 本用例会失败并提示同步补充实现与用例。</p>
     */
    @Test
    @DisplayName("原始SQL路径：不拼接条件（当前实现边界）")
    void shouldNotFilterRawSqlPath() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        StringBuilder sql = new StringBuilder("SELECT * FROM \"p_sys_user\" WHERE \"id\" = ?");
        this.dialect.prepareAuth(null, TABLE_NAME, sql, OperateType.SELECT);
        assertThat(sql.toString()).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"id\" = ?");
    }

    /**
     * 基于 {@link TableInfo} 的查询路径（{@code selectOneById} / {@code selectListByIds}，
     * 即 {@code getById}）当前不接入数据行权限<br/>
     * <p>守护用例：与原始 SQL 路径同理，此处固化「未接入」这一实现边界，
     * 避免误以为按主键查询也受数据行权限约束。</p>
     */
    @Test
    @DisplayName("TableInfo路径(getById)：不拼接条件（当前实现边界）")
    void shouldNotFilterTableInfoPath() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        StringBuilder sql = new StringBuilder("SELECT * FROM \"p_sys_user\" WHERE \"id\" = ?");
        this.dialect.prepareAuth(mock(TableInfo.class), sql, OperateType.SELECT);
        assertThat(sql.toString()).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"id\" = ?");
    }

    /**
     * 规则按当前租户加载：其他租户的规则不生效
     */
    @Test
    @DisplayName("租户隔离：其他租户的规则不生效")
    void shouldNotApplyOtherTenantRules() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        SecurityContextHolder.setTenantId(9999L);
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
        verify(this.permissionRowCache).getByTenantId(9999L);
    }

    /**
     * 无租户上下文（如白名单请求）时不抛异常且不拼接条件
     */
    @Test
    @DisplayName("无租户上下文：不拼接条件")
    void shouldSkipWhenTenantIdMissing() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        SecurityContextHolder.setTenantId(null);
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 无租户上下文时「所属部门及以下」不拼租户条件，绑定参数仅剩部门ID
     */
    @Test
    @DisplayName("无租户上下文：部门子树不拼租户条件")
    void shouldBuildDeptAndChildConditionWithoutTenant() {
        this.mockRulesForTenant(null,
                this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "2", null, null, null, null));
        SecurityContextHolder.setTenantId(null);
        assertThat(normalize(this.selectSql()))
                .contains("\"p_sys_user\".\"dept_id\" IN (")
                .doesNotContain("tenant_id");
        assertThat(this.selectParams()).containsExactly(DEPT_ID);
    }

    /**
     * 授权主体类型为空时不命中任何登录人
     */
    @Test
    @DisplayName("授权主体类型为空：不拼接条件")
    void shouldSkipWhenSubjectTypeBlank() {
        this.mockRules(this.rule(" ", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 授权主体为空时不命中任何登录人
     */
    @Test
    @DisplayName("授权主体为空：不拼接条件")
    void shouldSkipWhenPermissionSubjectBlank() {
        this.mockRules(this.rule("role", " ", TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 授权主体类型不在 role/dept/user 之内时不命中
     */
    @Test
    @DisplayName("授权主体类型未知：不拼接条件")
    void shouldSkipWhenSubjectTypeUnknown() {
        this.mockRules(this.rule("org", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 授权主体为部门但未命中当前登录人部门
     */
    @Test
    @DisplayName("授权主体为部门但未命中：不拼接条件")
    void shouldSkipWhenDeptSubjectNotMatched() {
        this.mockRules(this.rule("dept", "999999", TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 授权主体为用户但未命中当前登录人
     */
    @Test
    @DisplayName("授权主体为用户但未命中：不拼接条件")
    void shouldSkipWhenUserSubjectNotMatched() {
        this.mockRules(this.rule("user", "999999", TABLE_NAME, "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 「本人创建数据」规则已命中但登录人无用户ID（内部任务线程）时兜底拒绝
     */
    @Test
    @DisplayName("无登录人：本人创建数据兜底拒绝")
    void shouldDenyWhenUserIdMissing() {
        SecurityContextHolder.setUserId(null);
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "3", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 用户类型为空/未知时不属于管理员，不豁免数据行权限
     */
    @Test
    @DisplayName("用户类型未知：不豁免")
    void shouldNotSkipForUnknownUserType() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null));
        SecurityContextHolder.setUserType(null);
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ?");
    }

    /**
     * 授权客体匹配忽略大小写
     */
    @Test
    @DisplayName("授权客体大小写不敏感：命中")
    void shouldMatchPermissionObjectIgnoringCase() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), "P_SYS_User", "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ?");
    }

    /**
     * 授权客体支持逗号分隔多值
     */
    @Test
    @DisplayName("授权客体多值：任一命中即生效")
    void shouldMatchAnyPermissionObjectValue() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), "p_sys_dept, p_sys_user",
                "1", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" = ?");
    }

    /**
     * 多表查询时命中非首表的授权客体，条件以该表限定
     */
    @Test
    @DisplayName("多表查询：命中非首表")
    void shouldApplyRuleOnSecondQueryTable() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), "p_sys_dept", "1", null, null, null, null));
        QueryWrapper wrapper = QueryWrapper.create().from(TABLE_NAME, "p_sys_dept");
        assertThat(normalize(this.dialect.forSelectByQuery(wrapper)))
                .contains("WHERE \"p_sys_dept\".\"dept_id\" = ?");
    }

    /**
     * 关联表（JOIN）不在授权客体匹配范围内<br/>
     * <p>行为固化：方言只取 FROM 表（{@code CPI.getQueryTables}），JOIN 进来的表不参与授权客体匹配，
     * 针对关联表配置的规则会被静默忽略（不放行也不拒绝）。此用例用于暴露该边界，
     * 避免把规则配到关联表上却误以为已生效。</p>
     */
    @Test
    @DisplayName("关联表(JOIN)：规则被忽略（当前实现边界）")
    void shouldIgnoreRuleConfiguredOnJoinedTable() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), "p_sys_dept", "1", null, null, null, null));
        QueryWrapper wrapper = QueryWrapper.create().from(TABLE_NAME).as("u")
                .leftJoin("p_sys_dept").as("d").on("d.id = u.dept_id");
        String sql = normalize(this.dialect.forSelectByQuery(wrapper));
        assertThat(sql).contains("LEFT JOIN").contains("\"p_sys_dept\"");
        assertThat(sql).doesNotContain("WHERE");
    }

    /**
     * 授权范围为空时规则已命中但无法解析过滤条件，兜底拒绝
     */
    @Test
    @DisplayName("授权范围为空：兜底拒绝")
    void shouldDenyWhenPermissionScopeBlank() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "  ", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 授权范围取值未知（如脏数据）时兜底拒绝
     */
    @Test
    @DisplayName("授权范围未知：兜底拒绝")
    void shouldDenyWhenPermissionScopeUnknown() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "9", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 授权范围为自定义部门范围但未配置部门时兜底拒绝
     */
    @Test
    @DisplayName("自定义部门范围为空：兜底拒绝")
    void shouldDenyWhenCustomDeptScopeBlank() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "4", "  ", null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 自定义部门范围中的非法部门ID被忽略，不影响整条规则
     */
    @Test
    @DisplayName("自定义部门范围含非法值：忽略非法项")
    void shouldIgnoreIllegalDeptIdInCustomDeptScope() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "4", "11,abc,22", null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE \"dept_id\" IN (?, ?)");
        assertThat(this.selectParams()).containsExactly(11L, 22L);
    }

    /**
     * 授权范围为自定义字段但未配置权限字段时兜底拒绝
     */
    @Test
    @DisplayName("自定义字段-权限字段为空：兜底拒绝")
    void shouldDenyWhenColumnConditionBlank() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, " ", "eq", "1"));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 授权范围为自定义字段但未配置字段关系时兜底拒绝
     */
    @Test
    @DisplayName("自定义字段-字段关系为空：兜底拒绝")
    void shouldDenyWhenColumnRelationBlank() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "amount", " ", "1"));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 字段关系取值未知（如脏数据）时兜底拒绝
     */
    @Test
    @DisplayName("自定义字段-字段关系未知：兜底拒绝")
    void shouldDenyWhenColumnRelationUnknown() {
        this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "amount", "between", "1,2"));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0");
    }

    /**
     * 授权范围5-自定义字段：不等于、不包含与大小比较关系
     */
    @Test
    @DisplayName("授权范围5-自定义字段：不等于/不包含/大小比较")
    void shouldBuildCustomColumnOtherRelations() {
        // 字段关系 → 期望条件（均为字面量拼接，不占用绑定参数）
        Map<String, String> cases = new LinkedHashMap<>();
        cases.put("ne", "\"amount\" != E'20'");
        cases.put("not_like", "\"user_name\" NOT LIKE E'%admin%'");
        cases.put("gt", "\"amount\" > E'20'");
        cases.put("lt", "\"amount\" < E'20'");
        cases.put("ge", "\"amount\" >= E'20'");
        cases.put("le", "\"amount\" <= E'20'");
        for (Map.Entry<String, String> entry : cases.entrySet()) {
            String relation = entry.getKey();
            boolean textRelation = "not_like".equals(relation);
            this.mockRules(this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null,
                    textRelation ? "user_name" : "amount", relation, textRelation ? "admin" : "20"));
            assertThat(normalize(this.selectSql())).as("字段关系[%s]的拼接结果", relation)
                    .isEqualTo("SELECT * FROM \"p_sys_user\" WHERE " + entry.getValue());
            assertThat(this.selectParams()).as("字段关系[%s]不应占用绑定参数", relation).isEmpty();
        }
    }

    /**
     * 兜底拒绝的规则与正常规则按并集合并，不阻断其他规则放行的数据
     */
    @Test
    @DisplayName("多规则：兜底拒绝与命中规则并集")
    void shouldUnionDenyAllWithMatchedRule() {
        this.mockRules(
                this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "5", null, "1=1 OR id", "eq", "1"),
                this.rule("user", String.valueOf(USER_ID), TABLE_NAME, "3", null, null, null, null));
        assertThat(normalize(this.selectSql()))
                .isEqualTo("SELECT * FROM \"p_sys_user\" WHERE 1 = 0 OR \"create_id\" = ?");
    }

    /**
     * 命中的规则全部因授权客体缺少所需字段而跳过时不做任何限制<br/>
     * <p>行为固化：字段缺失走「跳过规则」而非「兜底拒绝」，因此当所有命中规则都被跳过时
     * 该查询不受数据行权限约束。此用例用于暴露该放开行为，便于评审是否需要改为拒绝。</p>
     */
    @Test
    @DisplayName("命中的规则全部跳过：不拼接条件（当前实现边界）")
    void shouldNotAppendWhenAllMatchedRulesSkipped() {
        when(this.columnProvider.hasColumn(anyString(), anyString())).thenReturn(false);
        this.mockRules(
                this.rule("role", String.valueOf(ROLE_ID), TABLE_NAME, "1", null, null, null, null),
                this.rule("user", String.valueOf(USER_ID), TABLE_NAME, "3", null, null, null, null));
        assertThat(normalize(this.selectSql())).isEqualTo("SELECT * FROM \"p_sys_user\"");
    }

    /**
     * 构建单表查询 SQL
     *
     * @return 生成的查询 SQL
     */
    private String selectSql() {
        return this.dialect.forSelectByQuery(QueryWrapper.create().from(TABLE_NAME));
    }

    /**
     * 构建单表查询 SQL 并返回绑定参数
     *
     * @return 绑定参数
     */
    private Object[] selectParams() {
        QueryWrapper wrapper = QueryWrapper.create().from(TABLE_NAME);
        this.dialect.forSelectByQuery(wrapper);
        return CPI.getValueArray(wrapper);
    }

    /**
     * 模拟缓存返回指定租户的规则
     *
     * @param rules 数据行权限规则
     */
    private void mockRules(PermissionRow... rules) {
        this.mockRulesForTenant(TENANT_ID, rules);
    }

    /**
     * 模拟缓存返回指定租户的数据行权限规则
     *
     * @param tenantId 租户ID
     * @param rules    数据行权限规则
     */
    private void mockRulesForTenant(Long tenantId, PermissionRow... rules) {
        when(this.permissionRowCache.getByTenantId(tenantId)).thenReturn(List.of(rules));
    }

    /**
     * 写入默认登录上下文（普通用户，部门/角色/租户齐全）
     */
    private void resetContext() {
        SecurityContextHolder.setUserId(USER_ID);
        SecurityContextHolder.setDeptId(DEPT_ID);
        SecurityContextHolder.setRoleIds(List.of(ROLE_ID));
        SecurityContextHolder.setUserType("normal");
        SecurityContextHolder.setTenantId(TENANT_ID);
    }

    /**
     * 构建数据行权限规则
     *
     * @param subjectType       授权主体类型
     * @param permissionSubject 授权主体
     * @param permissionObject  授权客体
     * @param permissionScope   授权范围
     * @param customDeptScope   自定义部门范围
     * @param columnCondition   自定义权限字段
     * @param columnRelation    自定义权限字段关系
     * @param columnValue       自定义权限字段值
     * @return 数据行权限规则
     */
    private PermissionRow rule(String subjectType, String permissionSubject, String permissionObject,
                               String permissionScope, String customDeptScope, String columnCondition,
                               String columnRelation, String columnValue) {
        PermissionRow rule = new PermissionRow();
        rule.setEnableFlag(Boolean.TRUE);
        rule.setSubjectType(subjectType);
        rule.setPermissionSubject(permissionSubject);
        rule.setPermissionObject(permissionObject);
        rule.setPermissionScope(permissionScope);
        rule.setCustomDeptScope(customDeptScope);
        rule.setColumnCondition(columnCondition);
        rule.setColumnRelation(columnRelation);
        rule.setColumnValue(columnValue);
        rule.setTenantId(TENANT_ID);
        return rule;
    }
}

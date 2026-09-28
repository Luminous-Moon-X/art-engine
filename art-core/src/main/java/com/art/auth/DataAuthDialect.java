package com.art.auth;

import cn.hutool.core.collection.CollectionUtil;
import com.art.auth.cache.PermissionRowCache;
import com.art.context.DataAuthContextHolder;
import com.art.domain.PermissionRow;
import com.art.enums.PermissionRowSubjectType;
import com.art.utils.SecurityUtil;
import com.art.utils.StringUtil;
import com.mybatisflex.core.constant.SqlConsts;
import com.mybatisflex.core.constant.SqlOperator;
import com.mybatisflex.core.dialect.KeywordWrap;
import com.mybatisflex.core.dialect.LimitOffsetProcessor;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.dialect.impl.CommonsDialectImpl;
import com.mybatisflex.core.query.CPI;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.query.RawQueryCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 数据权限处理类
 *
 * <p>在 SQL 构建阶段（{@code prepareAuth}）把当前登录人命中的数据行权限规则拼接为查询条件，
 * 规则语义见 {@link PermissionRow}：</p>
 * <ul>
 *     <li>授权主体：命中当前登录人的角色ID、部门ID或用户ID；</li>
 *     <li>授权客体：规则生效的表名，为空表示对所有表生效（作用于查询主表）；</li>
 *     <li>授权范围：1-所属部门 2-所属部门及以下 3-本人创建数据 4-自定义部门范围 5-自定义字段；</li>
 *     <li>多条命中规则之间按 <b>并集（OR）</b> 合并，即任一条规则放行的数据都可见。</li>
 * </ul>
 *
 * <p>另外两点约定：</p>
 * <ul>
 *     <li>超级管理员与租户管理员豁免数据行权限；</li>
 *     <li>「所属部门」「本人创建数据」等依赖于登录人部门/用户ID的规则，若登录人缺少对应属性，
 *     该规则不放行任何数据（{@code 1 = 0}），避免因属性缺失而越权放行；</li>
 *     <li>授权客体不存在该授权范围所依赖的字段（如 {@code dept_id}、{@code create_id}）时，
 *     跳过该规则的数据权限处理并打印告警，避免因字段不存在导致该表的全部查询失败。</li>
 * </ul>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Slf4j
public class DataAuthDialect extends CommonsDialectImpl {
    /**
     * 数据行权限已拼接标记<br/>
     * <p>
     * 同一个 {@link QueryWrapper} 可能被多次构建 SQL（例如先 {@code selectCountByQuery}
     * 再 {@code selectListByQuery}），而 {@code prepareAuth} 是"构建 SQL 时"的钩子，
     * 因此需要用上下文标记保证只拼接一次，避免条件与参数被重复追加。
     * 该方式与 MyBatis-Flex 自身 {@code TableInfo#appendConditions} 的幂等做法一致，
     * 且 {@link QueryWrapper#clone()} 会同时复制条件与上下文标记，分页的 count 查询克隆体
     * 因此不会漏加、也不会重复添加权限条件。
     * </p>
     */
    private static final String AUTH_APPLIED_FLAG = "dataAuthApplied";

    /**
     * 授权范围：所属部门
     */
    private static final String SCOPE_DEPT = "1";
    /**
     * 授权范围：所属部门及以下
     */
    private static final String SCOPE_DEPT_AND_CHILD = "2";
    /**
     * 授权范围：本人创建数据
     */
    private static final String SCOPE_SELF = "3";
    /**
     * 授权范围：自定义部门范围
     */
    private static final String SCOPE_CUSTOM_DEPT = "4";
    /**
     * 授权范围：自定义字段
     */
    private static final String SCOPE_CUSTOM_COLUMN = "5";

    /**
     * 字段关系：等于
     */
    private static final String RELATION_EQ = "eq";
    /**
     * 字段关系：不等于
     */
    private static final String RELATION_NE = "ne";
    /**
     * 字段关系：包含
     */
    private static final String RELATION_LIKE = "like";
    /**
     * 字段关系：不包含
     */
    private static final String RELATION_NOT_LIKE = "not_like";
    /**
     * 字段关系：为空
     */
    private static final String RELATION_NULL = "null";
    /**
     * 字段关系：不为空
     */
    private static final String RELATION_NOT_NULL = "not_null";
    /**
     * 字段关系：大于
     */
    private static final String RELATION_GT = "gt";
    /**
     * 字段关系：小于
     */
    private static final String RELATION_LT = "lt";
    /**
     * 字段关系：大于等于
     */
    private static final String RELATION_GE = "ge";
    /**
     * 字段关系：小于等于
     */
    private static final String RELATION_LE = "le";

    /**
     * 数据归属部门列（约定：授权范围为部门时，过滤目标表的该列）
     */
    private static final String COLUMN_DEPT_ID = "dept_id";
    /**
     * 数据创建人列（约定：授权范围为本人的，过滤目标表的该列）
     */
    private static final String COLUMN_CREATE_ID = "create_id";

    /**
     * 部门表（用于「所属部门及以下」的递归子树查询）
     */
    private static final String TABLE_DEPT = "p_sys_dept";
    /**
     * 部门表主键列
     */
    private static final String COLUMN_DEPT_TABLE_ID = "id";
    /**
     * 部门表上级部门列
     */
    private static final String COLUMN_DEPT_TABLE_PARENT_ID = "parent_id";
    /**
     * 部门子树递归 CTE 名称
     */
    private static final String DEPT_TREE_CTE = "data_auth_dept_tree";
    /**
     * 部门子树递归 CTE 内部部门表别名
     */
    private static final String DEPT_TREE_ALIAS = "d";
    /**
     * 部门子树递归 CTE 内部上级结果集别名
     */
    private static final String DEPT_TREE_PARENT_ALIAS = "t";
    /**
     * 逻辑删除标识列
     */
    private static final String COLUMN_DELETE_FLAG = "delete_flag";
    /**
     * 租户ID列
     */
    private static final String COLUMN_TENANT_ID = "tenant_id";
    /**
     * 逻辑删除-未删除值（与 MyBatis-Flex 默认逻辑删除处理器保持一致）
     */
    private static final String LOGIC_DELETE_NORMAL_VALUE = "0";

    /**
     * 恒不成立的条件，用于「规则已命中但无法解析出过滤条件」时兜底拒绝
     */
    private static final String DENY_ALL_CONDITION = "1 = 0";

    /**
     * 列名合法性校验<br/>
     * <p>「自定义字段」的列名会直接拼入 SQL，必须严格限制为合法标识符，防止 SQL 注入。</p>
     */
    private static final Pattern COLUMN_NAME_PATTERN = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

    /**
     * 数据行权限缓存提供者<br/>
     * <p>
     * 方言在 sqlSessionFactory 创建期间注册，此时缓存及其 Mapper 尚未（也不能）创建，
     * 因此只能持有延迟提供者，在真正构建 SQL 时再解析缓存实例。
     * </p>
     */
    private final ObjectProvider<PermissionRowCache> permissionRowCacheProvider;

    /**
     * 授权客体字段元数据提供者<br/>
     * <p>
     * 与缓存同理，方言注册时机早于业务 Bean，只能持有延迟提供者；
     * 由 art-system 的 {@code TableColumnCache} 实现（基于数据库系统目录 + 二级缓存）。
     * 元数据不可用（Bean 缺失或存在多个实现）时不做判定，保持原有拼接行为，
     * 避免因判定失败而放开数据。
     * </p>
     */
    private final ObjectProvider<DataAuthColumnProvider> dataAuthColumnProvider;

    /**
     * 构造函数
     */
    public DataAuthDialect(ObjectProvider<PermissionRowCache> permissionRowCacheProvider,
                           ObjectProvider<DataAuthColumnProvider> dataAuthColumnProvider) {
        super(KeywordWrap.DOUBLE_QUOTATION, LimitOffsetProcessor.POSTGRESQL);
        this.permissionRowCacheProvider = permissionRowCacheProvider;
        this.dataAuthColumnProvider = dataAuthColumnProvider;
    }

    /**
     * wrapper数据权限处理
     *
     * @param queryWrapper 查询条件
     * @param operateType  操作类型
     */
    @Override
    public void prepareAuth(QueryWrapper queryWrapper, OperateType operateType) {
        // 非查询操作，跳过
        if (!"SELECT".equals(operateType.name())) {
            super.prepareAuth(queryWrapper, operateType);
            return;
        }
        // 数据行权限规则自身的加载过程，跳过（避免读取缓存造成递归）
        if (DataAuthContextHolder.ignore()) {
            super.prepareAuth(queryWrapper, operateType);
            return;
        }
        // 超级管理员/租户管理员豁免数据行权限（与菜单权限对管理员放行全部权限的处理保持一致）
        if (this.isAdmin()) {
            super.prepareAuth(queryWrapper, operateType);
            return;
        }
        // 同一查询条件对象重复构建 SQL 时只拼接一次，避免条件与参数被重复追加
        if (Boolean.TRUE.equals(CPI.getContext(queryWrapper, AUTH_APPLIED_FLAG))) {
            super.prepareAuth(queryWrapper, operateType);
            return;
        }
        List<PermissionRow> permissionRows = permissionRowCacheProvider.getObject()
                .getByTenantId(SecurityUtil.getTenantId());
        // 当前租户没有数据权限规则，跳过
        if (CollectionUtil.isEmpty(permissionRows)) {
            super.prepareAuth(queryWrapper, operateType);
            return;
        }
        List<QueryTable> queryTables = CPI.getQueryTables(queryWrapper);
        // 未指定查询表，无法判断授权客体，跳过
        if (CollectionUtil.isEmpty(queryTables)) {
            super.prepareAuth(queryWrapper, operateType);
            return;
        }
        QueryCondition authCondition = this.buildAuthCondition(permissionRows, queryTables);
        if (authCondition != null) {
            CPI.putContext(queryWrapper, AUTH_APPLIED_FLAG, Boolean.TRUE);
            queryWrapper.and(authCondition);
        }
        super.prepareAuth(queryWrapper, operateType);
    }

    /**
     * sql数据权限处理<br/>
     * <p>
     * 该重载由「直接拼接 SQL 文本」的查询路径调用（如 {@code forSelectOneById}），
     * 此时无法拿到 {@link QueryWrapper} 与绑定参数，暂未接入数据行权限；
     * 当前数据行权限仅覆盖 {@link QueryWrapper} 查询路径（分页/列表/条件查询）。
     * </p>
     *
     * @param schema      数据库名
     * @param tableName   表名
     * @param sql         sql语句
     * @param operateType 操作类型
     */
    @Override
    public void prepareAuth(String schema, String tableName, StringBuilder sql, OperateType operateType) {

        super.prepareAuth(schema, tableName, sql, operateType);
    }

    /**
     * 构建当前登录人的数据行权限条件
     *
     * @param permissionRows 当前租户的数据行权限规则
     * @param queryTables    查询主表列表
     * @return 数据行权限条件；没有任何规则命中时返回 {@code null} 表示不做限制
     */
    private QueryCondition buildAuthCondition(List<PermissionRow> permissionRows, List<QueryTable> queryTables) {
        QueryCondition authCondition = null;
        for (PermissionRow permissionRow : permissionRows) {
            // 规则必须启用
            if (!Boolean.TRUE.equals(permissionRow.getEnableFlag())) {
                continue;
            }
            // 授权主体必须命中当前登录人
            if (!this.isSubjectMatched(permissionRow)) {
                continue;
            }
            // 授权客体必须命中当前查询的表
            QueryTable queryTable = this.matchQueryTable(permissionRow, queryTables);
            if (queryTable == null) {
                continue;
            }
            QueryCondition rowCondition = this.buildRowCondition(permissionRow, queryTable);
            if (rowCondition == null) {
                continue;
            }
            // 多条规则之间取并集
            authCondition = authCondition == null ? rowCondition : authCondition.or(rowCondition);
        }
        return authCondition;
    }

    /**
     * 判断规则授权主体是否命中当前登录人
     *
     * @param permissionRow 数据行权限规则
     * @return 是否命中
     */
    private boolean isSubjectMatched(PermissionRow permissionRow) {
        String subjectType = StringUtil.trim(permissionRow.getSubjectType());
        Set<String> permissionSubjects = this.splitToSet(permissionRow.getPermissionSubject());
        if (StringUtil.isBlank(subjectType) || permissionSubjects.isEmpty()) {
            return false;
        }
        if (PermissionRowSubjectType.ROLE.getCode().equals(subjectType)) {
            return SecurityUtil.getRoleId().stream()
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .anyMatch(permissionSubjects::contains);
        }
        if (PermissionRowSubjectType.DEPT.getCode().equals(subjectType)) {
            Long deptId = SecurityUtil.getDeptId();
            return deptId != null && permissionSubjects.contains(String.valueOf(deptId));
        }
        if (PermissionRowSubjectType.USER.getCode().equals(subjectType)) {
            Long userId = SecurityUtil.getUserId();
            return userId != null && permissionSubjects.contains(String.valueOf(userId));
        }
        return false;
    }

    /**
     * 匹配规则授权客体对应的查询表
     *
     * @param permissionRow 数据行权限规则
     * @param queryTables   查询主表列表
     * @return 命中的查询表；未命中返回 {@code null}
     */
    private QueryTable matchQueryTable(PermissionRow permissionRow, List<QueryTable> queryTables) {
        String permissionObject = permissionRow.getPermissionObject();
        // 授权客体为空：对所有数据表生效，作用于查询主表（FROM 的第一个表）
        if (StringUtil.isBlank(permissionObject)) {
            return queryTables.getFirst();
        }
        Set<String> permissionObjects = new HashSet<>();
        this.splitToSet(permissionObject).forEach(object -> permissionObjects.add(object.toLowerCase()));
        for (QueryTable queryTable : queryTables) {
            String tableName = queryTable.getName();
            if (StringUtil.isNotBlank(tableName) && permissionObjects.contains(tableName.toLowerCase())) {
                return queryTable;
            }
        }
        return null;
    }

    /**
     * 按授权范围构建单条规则的过滤条件
     *
     * @param permissionRow 数据行权限规则
     * @param queryTable    规则生效的查询表
     * @return 过滤条件；规则已命中但无法解析时返回「恒不成立」条件兜底拒绝
     */
    private QueryCondition buildRowCondition(PermissionRow permissionRow, QueryTable queryTable) {
        String permissionScope = StringUtil.trim(permissionRow.getPermissionScope());
        if (StringUtil.isBlank(permissionScope)) {
            return denyAll();
        }
        return switch (permissionScope) {
            case SCOPE_DEPT -> this.buildDeptCondition(permissionRow, queryTable);
            case SCOPE_DEPT_AND_CHILD -> this.buildDeptAndChildCondition(permissionRow, queryTable);
            case SCOPE_SELF -> this.buildSelfCondition(permissionRow, queryTable);
            case SCOPE_CUSTOM_DEPT -> this.buildCustomDeptCondition(permissionRow, queryTable);
            case SCOPE_CUSTOM_COLUMN -> this.buildCustomColumnCondition(permissionRow, queryTable);
            default -> denyAll();
        };
    }

    /**
     * 校验授权客体是否包含授权范围所依赖的字段
     *
     * <p>授权客体由管理员从数据库全表清单中选择，可能存在不含 {@code dept_id}、{@code create_id}
     * 等字段的表；此时若继续拼接条件，该表的全部查询都会因「字段不存在」而失败，
     * 因此约定：字段不存在时跳过该规则的数据权限处理并打印告警。</p>
     *
     * @param permissionRow 数据行权限规则
     * @param queryTable    规则生效的查询表
     * @param columnName    授权范围依赖的字段名
     * @return 是否继续拼接条件；字段不存在时返回 {@code false} 表示跳过该规则
     */
    private boolean checkColumnExists(PermissionRow permissionRow, QueryTable queryTable, String columnName) {
        DataAuthColumnProvider columnProvider = dataAuthColumnProvider.getIfUnique();
        // 元数据不可用时无法判定字段是否存在，保持原有拼接行为（报错可见），避免静默放开数据
        if (columnProvider == null) {
            return true;
        }
        String tableName = queryTable == null ? null : queryTable.getName();
        // 无法取到真实表名（如子查询派生表），同样不做判定，保持原有拼接行为
        if (StringUtil.isBlank(tableName)) {
            return true;
        }
        if (columnProvider.hasColumn(tableName, columnName)) {
            return true;
        }
        log.warn("数据权限规则[{}]的授权客体[{}]不存在字段[{}]，已跳过该规则的数据权限处理",
                permissionRow.getId(), tableName, columnName);
        return false;
    }

    /**
     * 构建「所属部门」条件：目标表 dept_id = 当前登录人部门ID
     *
     * @param permissionRow 数据行权限规则
     * @param queryTable    规则生效的查询表
     * @return 过滤条件；授权客体无 dept_id 字段时返回 {@code null} 表示跳过该规则
     */
    private QueryCondition buildDeptCondition(PermissionRow permissionRow, QueryTable queryTable) {
        Long deptId = SecurityUtil.getDeptId();
        // 登录人没有归属部门，无法确定数据范围，拒绝该规则放行的全部数据
        if (deptId == null) {
            return denyAll();
        }
        if (!this.checkColumnExists(permissionRow, queryTable, COLUMN_DEPT_ID)) {
            return null;
        }
        return QueryCondition.create(new QueryColumn(queryTable, COLUMN_DEPT_ID), SqlOperator.EQUALS, deptId);
    }

    /**
     * 构建「所属部门及以下」条件：目标表 dept_id 属于当前登录人部门及其全部下级部门
     *
     * <p>部门树仅有 parent_id，无法通过单层条件表达，因此拼接 PostgreSQL 递归子查询
     * （{@code WITH RECURSIVE}）在数据库侧展开子树。</p>
     *
     * @param permissionRow 数据行权限规则
     * @param queryTable    规则生效的查询表
     * @return 过滤条件；授权客体无 dept_id 字段时返回 {@code null} 表示跳过该规则
     */
    private QueryCondition buildDeptAndChildCondition(PermissionRow permissionRow, QueryTable queryTable) {
        Long deptId = SecurityUtil.getDeptId();
        // 登录人没有归属部门，无法确定数据范围，拒绝该规则放行的全部数据
        if (deptId == null) {
            return denyAll();
        }
        if (!this.checkColumnExists(permissionRow, queryTable, COLUMN_DEPT_ID)) {
            return null;
        }
        String idColumn = wrap(COLUMN_DEPT_TABLE_ID);
        String deleteFlagCondition = wrap(COLUMN_DELETE_FLAG) + " = " + LOGIC_DELETE_NORMAL_VALUE;
        String deptTable = wrap(getRealTable(TABLE_DEPT, OperateType.SELECT));
        String cteName = wrap(DEPT_TREE_CTE);
        String deptAlias = wrap(DEPT_TREE_ALIAS);
        String parentAlias = wrap(DEPT_TREE_PARENT_ALIAS);

        List<Object> params = new ArrayList<>();
        StringBuilder deptTree = new StringBuilder();
        // 非递归项：当前登录人所属部门
        deptTree.append(SqlConsts.WITH).append("RECURSIVE ").append(cteName).append(" AS (")
                .append(SqlConsts.SELECT).append(idColumn)
                .append(SqlConsts.FROM).append(deptTable)
                .append(SqlConsts.WHERE).append(idColumn).append(SqlConsts.EQUALS_PLACEHOLDER)
                .append(SqlConsts.AND).append(deleteFlagCondition);
        params.add(deptId);
        // 租户条件：部门ID为雪花ID本身已全局唯一，此处仅作为跨租户脏数据的额外兜底
        Long tenantId = SecurityUtil.getTenantId();
        if (tenantId != null) {
            deptTree.append(SqlConsts.AND).append(wrap(COLUMN_TENANT_ID)).append(SqlConsts.EQUALS_PLACEHOLDER);
            params.add(tenantId);
        }
        // 递归项：以上一步结果集为父部门的部门
        deptTree.append(SqlConsts.UNION_ALL)
                .append(SqlConsts.SELECT).append(deptAlias).append(SqlConsts.REFERENCE).append(idColumn)
                .append(SqlConsts.FROM).append(deptTable).append(SqlConsts.AS).append(deptAlias)
                .append(SqlConsts.INNER_JOIN).append(cteName).append(SqlConsts.AS).append(parentAlias)
                .append(SqlConsts.ON).append(deptAlias).append(SqlConsts.REFERENCE).append(wrap(COLUMN_DEPT_TABLE_PARENT_ID))
                .append(SqlConsts.EQUALS).append(parentAlias).append(SqlConsts.REFERENCE).append(idColumn)
                .append(SqlConsts.WHERE).append(deptAlias).append(SqlConsts.REFERENCE).append(deleteFlagCondition);
        if (tenantId != null) {
            deptTree.append(SqlConsts.AND).append(deptAlias).append(SqlConsts.REFERENCE)
                    .append(wrap(COLUMN_TENANT_ID)).append(SqlConsts.EQUALS_PLACEHOLDER);
            params.add(tenantId);
        }
        deptTree.append(SqlConsts.BRACKET_RIGHT).append(SqlConsts.BLANK).append(SqlConsts.SELECT)
                .append(idColumn).append(SqlConsts.FROM).append(cteName);

        String conditionSql = this.buildColumnSql(queryTable, COLUMN_DEPT_ID)
                + SqlConsts.IN + SqlConsts.BRACKET_LEFT + deptTree + SqlConsts.BRACKET_RIGHT;
        return new RawQueryCondition(conditionSql, params.toArray());
    }

    /**
     * 构建「本人创建数据」条件：目标表 create_id = 当前登录人ID
     *
     * @param permissionRow 数据行权限规则
     * @param queryTable    规则生效的查询表
     * @return 过滤条件；授权客体无 create_id 字段时返回 {@code null} 表示跳过该规则
     */
    private QueryCondition buildSelfCondition(PermissionRow permissionRow, QueryTable queryTable) {
        Long userId = SecurityUtil.getUserId();
        // 无登录人信息（如内部任务线程），无法确定数据范围，拒绝该规则放行的全部数据
        if (userId == null) {
            return denyAll();
        }
        if (!this.checkColumnExists(permissionRow, queryTable, COLUMN_CREATE_ID)) {
            return null;
        }
        return QueryCondition.create(new QueryColumn(queryTable, COLUMN_CREATE_ID), SqlOperator.EQUALS, userId);
    }

    /**
     * 构建「自定义部门范围」条件：目标表 dept_id 属于规则配置的部门集合
     *
     * @param permissionRow 数据行权限规则
     * @param queryTable    规则生效的查询表
     * @return 过滤条件；授权客体无 dept_id 字段时返回 {@code null} 表示跳过该规则
     */
    private QueryCondition buildCustomDeptCondition(PermissionRow permissionRow, QueryTable queryTable) {
        List<Long> deptIds = this.splitToLongs(permissionRow.getCustomDeptScope());
        if (deptIds.isEmpty()) {
            return denyAll();
        }
        if (!this.checkColumnExists(permissionRow, queryTable, COLUMN_DEPT_ID)) {
            return null;
        }
        return QueryCondition.create(new QueryColumn(queryTable, COLUMN_DEPT_ID), SqlConsts.IN, deptIds);
    }

    /**
     * 构建「自定义字段」条件：目标表指定字段按配置关系与配置值过滤
     *
     * <p>字段值以转义后的 SQL 字面量拼接而非占位符绑定：规则值统一存放于 varchar 列，
     * 无法得知目标字段的真实类型，绑定为 varchar 参数会导致数值型字段报
     * {@code operator does not exist: bigint = character varying}；使用字面量时
     * PostgreSQL 会按字段类型自动推断（unknown 类型），数值型与字符型字段均可正常比较。</p>
     *
     * @param permissionRow 数据行权限规则
     * @param queryTable    规则生效的查询表
     * @return 过滤条件；授权客体无该权限字段时返回 {@code null} 表示跳过该规则
     */
    private QueryCondition buildCustomColumnCondition(PermissionRow permissionRow, QueryTable queryTable) {
        String columnName = StringUtil.trim(permissionRow.getColumnCondition());
        String columnRelation = StringUtil.trim(permissionRow.getColumnRelation());
        if (StringUtil.isBlank(columnName) || StringUtil.isBlank(columnRelation)) {
            return denyAll();
        }
        // 列名直接拼入 SQL，必须严格校验为合法标识符
        if (!COLUMN_NAME_PATTERN.matcher(columnName).matches()) {
            return denyAll();
        }
        if (!this.checkColumnExists(permissionRow, queryTable, columnName)) {
            return null;
        }
        String columnValue = permissionRow.getColumnValue();
        // 「为空」「不为空」之外的关系必须配置字段值，否则视为无效规则
        // （必须在拼接通配符之前判断，否则 null 会被拼成 "%null%" 而绕过校验）
        boolean needColumnValue = !RELATION_NULL.equals(columnRelation) && !RELATION_NOT_NULL.equals(columnRelation);
        if (needColumnValue && StringUtil.isBlank(columnValue)) {
            return denyAll();
        }
        QueryColumn queryColumn = new QueryColumn(queryTable, columnName);
        return switch (columnRelation) {
            case RELATION_EQ -> createValueCondition(queryColumn, SqlOperator.EQUALS, columnValue);
            case RELATION_NE -> createValueCondition(queryColumn, SqlOperator.NOT_EQUALS, columnValue);
            case RELATION_LIKE -> createValueCondition(queryColumn, SqlOperator.LIKE, "%" + columnValue + "%");
            case RELATION_NOT_LIKE -> createValueCondition(queryColumn, SqlOperator.NOT_LIKE, "%" + columnValue + "%");
            case RELATION_NULL -> QueryCondition.create(queryColumn, SqlOperator.IS_NULL, null);
            case RELATION_NOT_NULL -> QueryCondition.create(queryColumn, SqlOperator.IS_NOT_NULL, null);
            case RELATION_GT -> createValueCondition(queryColumn, SqlOperator.GT, columnValue);
            case RELATION_LT -> createValueCondition(queryColumn, SqlOperator.LT, columnValue);
            case RELATION_GE -> createValueCondition(queryColumn, SqlOperator.GE, columnValue);
            case RELATION_LE -> createValueCondition(queryColumn, SqlOperator.LE, columnValue);
            default -> denyAll();
        };
    }

    /**
     * 构建带字段值的过滤条件（字段值以转义字面量拼接）
     *
     * @param queryColumn 过滤字段
     * @param sqlOperator 关系运算符
     * @param value       字段值
     * @return 过滤条件
     */
    private QueryCondition createValueCondition(QueryColumn queryColumn, SqlOperator sqlOperator, String value) {
        return QueryCondition.create(queryColumn, sqlOperator, new RawQueryCondition(escapeLiteral(value)));
    }

    /**
     * 判断当前登录人是否为管理员（超级管理员/租户管理员），管理员豁免数据行权限
     *
     * @return 是否为管理员
     */
    private boolean isAdmin() {
        return SecurityUtil.isSuperAdmin() || SecurityUtil.isTenantAdmin();
    }

    /**
     * 构建恒不成立的过滤条件<br/>
     * <p>规则已命中当前登录人与查询表，但配置缺失或登录人缺少必要属性时，
     * 该规则放行的数据范围无法确定，按最小权限原则不放行任何数据。</p>
     *
     * @return 恒不成立的过滤条件
     */
    private static QueryCondition denyAll() {
        return new RawQueryCondition(DENY_ALL_CONDITION);
    }

    /**
     * 构建查询表指定字段的 SQL 引用（含表别名/schema 处理，与 {@code QueryColumn#toConditionSql} 保持一致）
     *
     * @param queryTable 查询表
     * @param columnName 字段名
     * @return 字段 SQL 引用
     */
    private String buildColumnSql(QueryTable queryTable, String columnName) {
        if (queryTable == null) {
            return wrap(columnName);
        }
        String alias = queryTable.getAlias();
        if (StringUtil.isNotBlank(alias)) {
            return wrap(alias) + SqlConsts.REFERENCE + wrap(columnName);
        }
        String tableName = queryTable.getName();
        if (StringUtil.isBlank(tableName)) {
            return wrap(columnName);
        }
        String realTable = getRealTable(tableName, OperateType.SELECT);
        String schema = queryTable.getSchema();
        if (StringUtil.isNotBlank(schema)) {
            String realSchema = getRealSchema(schema, realTable, OperateType.SELECT);
            return wrap(realSchema) + SqlConsts.REFERENCE + wrap(realTable) + SqlConsts.REFERENCE + wrap(columnName);
        }
        return wrap(realTable) + SqlConsts.REFERENCE + wrap(columnName);
    }

    /**
     * 转义为 PostgreSQL 字面量<br/>
     * <p>使用 {@code E''} 转义字符串语法：先转义反斜杠再转义单引号，
     * 保证在 {@code standard_conforming_strings} 任意取值下都不会越出字面量，从而杜绝 SQL 注入。</p>
     *
     * @param value 原始值
     * @return 转义后的 SQL 字面量
     */
    private static String escapeLiteral(String value) {
        String text = value == null ? "" : value;
        return "E'" + text.replace("\\", "\\\\").replace("'", "\\'") + "'";
    }

    /**
     * 拆分逗号分隔字符串为集合（去除空白项）
     *
     * @param value 逗号分隔字符串
     * @return 拆分结果
     */
    private Set<String> splitToSet(String value) {
        if (StringUtil.isBlank(value)) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        for (String item : value.split(",")) {
            String trimmed = StringUtil.trim(item);
            if (StringUtil.isNotBlank(trimmed)) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * 拆分逗号分隔字符串为部门ID集合（忽略非法项）
     *
     * @param value 逗号分隔的部门ID字符串
     * @return 部门ID集合
     */
    private List<Long> splitToLongs(String value) {
        if (StringUtil.isBlank(value)) {
            return Collections.emptyList();
        }
        List<Long> result = new ArrayList<>();
        for (String item : value.split(",")) {
            String trimmed = StringUtil.trim(item);
            if (StringUtil.isBlank(trimmed)) {
                continue;
            }
            try {
                result.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ignored) {
                // 单条脏数据不影响整条规则，忽略非法部门ID
            }
        }
        return result;
    }
}

package com.art.auth;

import cn.hutool.core.collection.CollectionUtil;
import com.art.auth.cache.PermissionRowCache;
import com.art.context.DataAuthContextHolder;
import com.art.domain.PermissionRow;
import com.art.utils.SecurityUtil;
import com.mybatisflex.core.dialect.KeywordWrap;
import com.mybatisflex.core.dialect.LimitOffsetProcessor;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.dialect.impl.CommonsDialectImpl;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

/**
 * 数据权限处理类
 *
 * @author Luminous.X
 * @since 2.1.0
 */
public class DataAuthDialect extends CommonsDialectImpl {
    /**
     * 数据行权限缓存提供者<br/>
     * <p>
     * 方言在 sqlSessionFactory 创建期间注册，此时缓存及其 Mapper 尚未（也不能）创建，
     * 因此只能持有延迟提供者，在真正构建 SQL 时再解析缓存实例。
     * </p>
     */
    private final ObjectProvider<PermissionRowCache> permissionRowCacheProvider;

    /**
     * 构造函数
     */
    public DataAuthDialect(ObjectProvider<PermissionRowCache> permissionRowCacheProvider) {
        super(KeywordWrap.DOUBLE_QUOTATION, LimitOffsetProcessor.POSTGRESQL);
        this.permissionRowCacheProvider = permissionRowCacheProvider;
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
        List<PermissionRow> permissionRows = permissionRowCacheProvider.getObject()
                .getByTenantId(SecurityUtil.getTenantId());
        // 当前租户没有数据权限规则，跳过
        if (CollectionUtil.isEmpty(permissionRows)) {
            super.prepareAuth(queryWrapper, operateType);
            return;
        }
//        // 获取查询表列表
//        List<QueryTable> queryTables = CPI.getQueryTables(queryWrapper);
//        for (PermissionRow permissionRow : permissionRows) {
//            String subjectType = permissionRow.getSubjectType();
//        }
        super.prepareAuth(queryWrapper, operateType);
    }

    /**
     * sql数据权限处理
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
}

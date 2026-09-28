package com.art.cache;

import com.art.auth.DataAuthColumnProvider;
import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.utils.StringUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据库表字段元数据二级缓存实现（数据行权限使用，归属 art-system）
 *
 * <p>数据行权限在构建 SQL 时需要判断授权客体是否包含该授权范围依赖的字段
 * （见 {@link DataAuthColumnProvider}），该判断位于查询构建的热路径上，
 * 因此字段元数据整体缓存，避免每次查询都访问数据库系统目录。</p>
 *
 * <p>元数据来自 PostgreSQL 系统目录 {@code pg_attribute}，与
 * {@code TableMetadataServiceImpl} 中「授权客体/权限字段下拉」使用的是同一数据源，
 * 因此对无实体类的表同样准确，不依赖 ORM 的表名到实体映射。</p>
 *
 * <p>表结构变更（DDL）不经过业务写入，因此不做主动刷新，依赖缓存自身的
 * L1/L2 过期时间（{@code art.cache.l1-ttl-seconds} / {@code art.cache.l2-ttl-seconds}）
 * 在到期后重新加载。</p>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Component
public class TableColumnCache extends ArtCache<Map<String, Set<String>>> implements DataAuthColumnProvider {

    /**
     * 查询当前 schema 下所有普通表的字段清单（表名与字段名统一按小写归集）
     */
    private static final String SELECT_ALL_TABLE_COLUMN_SQL = """
            SELECT c.relname AS table_name,
                   a.attname AS column_name
            FROM pg_attribute a
                     JOIN pg_class c ON c.oid = a.attrelid
                     JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE c.relkind = 'r'
              AND n.nspname = current_schema()
              AND a.attnum > 0
              AND NOT a.attisdropped
            """;

    /**
     * JDBC 操作模板
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param properties    缓存配置
     * @param jdbcTemplate  JDBC操作模板
     */
    public TableColumnCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties,
                            JdbcTemplate jdbcTemplate) {
        super(redisTemplate, properties);
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 从数据库加载字段元数据
     *
     * @return 表名（小写）→ 字段名集合（小写）
     */
    @Override
    protected Map<String, Set<String>> loadFromDb() {
        // 使用 JDBC 直接查询系统目录，不经过 MyBatis-Flex 方言，因此不会触发数据行权限处理
        List<Map<String, Object>> rows = this.jdbcTemplate.queryForList(SELECT_ALL_TABLE_COLUMN_SQL);
        Map<String, Set<String>> tableColumns = new HashMap<>(rows.size());
        for (Map<String, Object> row : rows) {
            String tableName = String.valueOf(row.get("table_name")).toLowerCase();
            String columnName = String.valueOf(row.get("column_name")).toLowerCase();
            tableColumns.computeIfAbsent(tableName, key -> new HashSet<>()).add(columnName);
        }
        return tableColumns;
    }

    /**
     * 判断指定表是否存在指定字段（忽略大小写）
     *
     * @param tableName  表名
     * @param columnName 字段名
     * @return 是否存在
     */
    @Override
    public boolean hasColumn(String tableName, String columnName) {
        if (StringUtil.isBlank(tableName) || StringUtil.isBlank(columnName)) {
            return false;
        }
        Set<String> columns = get().get(tableName.trim().toLowerCase());
        return columns != null && columns.contains(columnName.trim().toLowerCase());
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    public String cacheName() {
        return "数据库表字段元数据";
    }

    /**
     * 获取缓存Key
     *
     * @return 缓存Key
     */
    @Override
    public String redisKey() {
        return "tableColumn";
    }
}

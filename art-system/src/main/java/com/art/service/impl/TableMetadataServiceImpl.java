package com.art.service.impl;

import com.art.domain.vo.PermissionTableColumnVO;
import com.art.domain.vo.PermissionTableVO;
import com.art.service.TableMetadataService;
import com.art.utils.StringUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 数据库表元数据服务实现类
 *
 * <p>表与字段元数据直接读取 PostgreSQL 系统目录（pg_class / pg_attribute），
 * 仅查询当前 schema，表名通过占位符绑定，避免 SQL 注入。</p>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Service
public class TableMetadataServiceImpl implements TableMetadataService {

    /**
     * 查询当前 schema 下所有普通表及表备注
     */
    private static final String SELECT_TABLE_LIST_SQL = """
            SELECT c.relname AS table_name,
                   COALESCE(obj_description(c.oid, 'pg_class'), '') AS table_comment
            FROM pg_class c
                     JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE c.relkind = 'r'
              AND n.nspname = current_schema()
            ORDER BY c.relname
            """;

    /**
     * 查询指定表的字段及字段备注
     */
    private static final String SELECT_TABLE_COLUMN_SQL = """
            SELECT a.attname AS column_name,
                   COALESCE(col_description(a.attrelid, a.attnum), '') AS column_comment
            FROM pg_attribute a
                     JOIN pg_class c ON c.oid = a.attrelid
                     JOIN pg_namespace n ON n.oid = c.relnamespace
            WHERE c.relname = ?
              AND n.nspname = current_schema()
              AND a.attnum > 0
              AND NOT a.attisdropped
            ORDER BY a.attnum
            """;

    /**
     * JDBC 操作模板
     */
    private final JdbcTemplate jdbcTemplate;

    /**
     * 构造函数
     *
     * @param jdbcTemplate JDBC 操作模板
     */
    public TableMetadataServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 查询当前数据库中的所有表（含表备注）
     *
     * @return 表信息列表
     */
    @Override
    public List<PermissionTableVO> selectTableList() {
        return this.jdbcTemplate.query(SELECT_TABLE_LIST_SQL, (rs, rowNum) -> {
            PermissionTableVO vo = new PermissionTableVO();
            vo.setTableName(rs.getString("table_name"));
            vo.setTableComment(rs.getString("table_comment"));
            return vo;
        });
    }

    /**
     * 查询指定表的字段列表（含字段备注）
     *
     * @param tableName 表名
     * @return 字段信息列表，表不存在时返回空列表
     */
    @Override
    public List<PermissionTableColumnVO> selectTableColumnList(String tableName) {
        if (StringUtil.isBlank(tableName)) {
            return Collections.emptyList();
        }
        return this.jdbcTemplate.query(SELECT_TABLE_COLUMN_SQL, (rs, rowNum) -> {
            PermissionTableColumnVO vo = new PermissionTableColumnVO();
            vo.setColumnName(rs.getString("column_name"));
            vo.setColumnComment(rs.getString("column_comment"));
            return vo;
        }, tableName.trim());
    }
}

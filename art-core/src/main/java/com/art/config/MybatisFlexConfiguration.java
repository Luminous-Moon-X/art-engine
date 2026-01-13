package com.art.config;

import com.art.context.IgnoreSqlLogContextHolder;
import com.mybatisflex.core.audit.AuditManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Flex 配置类<br/>
 * 用于配置 MyBatis-Flex 的审计功能，实现 SQL 执行日志记录和慢查询监控
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
public class MybatisFlexConfiguration {
    // 用于记录 MyBatis-Flex SQL 日志的专用 Logger
    private static final Logger logger = LoggerFactory.getLogger("mybatis-flex-sql");
    // SQL 分隔线，用于在日志中分隔不同的 SQL 记录
    private static final String SQL_SEPARATOR = "=".repeat(80);

    /**
     * 构造函数 - 启用并配置 MyBatis-Flex 审计功能<br/>
     * 设置审计启用状态，并配置消息收集器以记录 SQL 执行详情，包括操作类型、执行时间和性能警告
     */
    public MybatisFlexConfiguration() {
        // 启用审计功能
        AuditManager.setAuditEnable(true);
        // 设置消息收集器，用于记录 SQL 执行信息
        AuditManager.setMessageCollector(auditMessage -> {
            // 如果当前线程已启用忽略 SQL 日志，则不记录日志
            if (IgnoreSqlLogContextHolder.ignore() != null && IgnoreSqlLogContextHolder.ignore()) {
                return;
            }
            // 获取完整 SQL 语句
            String sql = auditMessage.getFullSql();
            // 获取执行耗时（毫秒）
            long elapsedTime = auditMessage.getElapsedTime();
            // 提取 SQL 操作类型（SELECT, INSERT, UPDATE, DELETE 等）
            String operationType = extractOperationType(sql);
            // 如果执行时间超过 1000ms，则标记为慢 SQL
            String warning = elapsedTime > 1000 ? " [⚠️ 慢SQL]" : "";
            // 记录格式化的 SQL 执行日志
            logger.info("\n{}\n[{}] {} | 耗时: {}ms{}\n{}\n",
                    SQL_SEPARATOR,           // 分隔线
                    operationType,           // SQL 操作类型
                    formatTime(),            // 执行时间
                    elapsedTime,             // 执行耗时
                    warning,                 // 性能警告
                    formatSql(sql)           // 格式化后的 SQL 语句
            );
        });
    }

    /**
     * 从 SQL 语句中提取操作类型<br/>
     * 识别常见的 SQL 操作类型，如 SELECT、INSERT、UPDATE、DELETE 和 DDL 语句
     *
     * @param sql 待分析的 SQL 语句
     * @return 操作类型标识（SELECT, INSERT, UPDATE, DELETE, DDL, OTHER 或 UNKNOWN）
     */
    private String extractOperationType(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return "UNKNOWN"; // SQL 为空或无效时返回 UNKNOWN
        }
        String upperSql = sql.trim().toUpperCase(); // 将 SQL 转换为大写以便匹配
        if (upperSql.startsWith("SELECT")) return "SELECT"; // 查询操作
        if (upperSql.startsWith("INSERT")) return "INSERT"; // 插入操作
        if (upperSql.startsWith("UPDATE")) return "UPDATE"; // 更新操作
        if (upperSql.startsWith("DELETE")) return "DELETE"; // 删除操作
        if (upperSql.startsWith("CREATE")) return "DDL";    // 数据定义语言（创建表等）
        if (upperSql.startsWith("ALTER")) return "DDL";     // 数据定义语言（修改表结构等）
        if (upperSql.startsWith("DROP")) return "DDL";      // 数据定义语言（删除表等）
        return "OTHER"; // 其他未明确分类的操作类型
    }

    /**
     * 格式化当前时间为指定格式<br/>
     * 返回格式为 "HH:mm:ss.SSS" 的时间字符串
     *
     * @return 格式化后的时间字符串
     */
    private String formatTime() {
        return java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        );
    }

    /**
     * 格式化 SQL 语句<br/>
     * 将 SQL 语句中的多个连续空白字符替换为单个空格，并去除首尾空白
     *
     * @param sql 待格式化的 SQL 语句
     * @return 格式化后的 SQL 语句
     */
    private String formatSql(String sql) {
        if (sql == null) {
            return ""; // SQL 为 null 时返回空字符串
        }
        return sql.replaceAll("\\s+", " ").trim(); // 将多个空白字符替换为单个空格并去除首尾空白
    }
}

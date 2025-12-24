package com.art.art.config;


import com.mybatisflex.core.audit.AuditManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * MybatisFlex配置类
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Configuration
public class MybatisFlexConfiguration {
    private static final Logger logger = LoggerFactory.getLogger("mybatis-flex-sql");

    public MybatisFlexConfiguration() {
        //开启审计功能
        AuditManager.setAuditEnable(true);
        //设置 SQL 审计收集器
        AuditManager.setMessageCollector(auditMessage ->
                logger.info("{},{}ms", auditMessage.getFullSql()
                        , auditMessage.getElapsedTime())
        );
    }
}

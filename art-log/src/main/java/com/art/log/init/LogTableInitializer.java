package com.art.log.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 日志表自动初始化器
 * <p>
 * 应用启动时自动检查并创建日志相关数据库表
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogTableInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createLoginLogTable();
        createMenuLogTable();
        createApiLogTable();
        log.info("日志表初始化完成");
    }

    /**
     * 创建登录日志表
     */
    private void createLoginLogTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS `p_sys_login_log` (
                  `id` bigint NOT NULL COMMENT '主键',
                  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名',
                  `nick_name` varchar(64) DEFAULT NULL COMMENT '用户昵称',
                  `login_ip` varchar(64) DEFAULT NULL COMMENT '登录IP',
                  `login_time` datetime DEFAULT NULL COMMENT '登录时间',
                  `browser` varchar(64) DEFAULT NULL COMMENT '浏览器',
                  `os` varchar(64) DEFAULT NULL COMMENT '操作系统',
                  `status` tinyint DEFAULT 1 COMMENT '登录状态(1成功 0失败)',
                  `message` varchar(256) DEFAULT NULL COMMENT '提示信息',
                  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                  `create_id` bigint DEFAULT NULL COMMENT '创建人',
                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                  `update_id` bigint DEFAULT NULL COMMENT '修改人',
                  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标识',
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表'
                """;
        jdbcTemplate.execute(sql);
        log.info("登录日志表[p_sys_login_log]初始化成功");
    }

    /**
     * 创建菜单日志表
     */
    private void createMenuLogTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS `p_sys_menu_log` (
                  `id` bigint NOT NULL COMMENT '主键',
                  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
                  `user_name` varchar(64) DEFAULT NULL COMMENT '用户名',
                  `nick_name` varchar(64) DEFAULT NULL COMMENT '用户昵称',
                  `menu_name` varchar(128) DEFAULT NULL COMMENT '菜单名称',
                  `menu_path` varchar(256) DEFAULT NULL COMMENT '菜单路径',
                  `click_time` datetime DEFAULT NULL COMMENT '点击时间',
                  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                  `create_id` bigint DEFAULT NULL COMMENT '创建人',
                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                  `update_id` bigint DEFAULT NULL COMMENT '修改人',
                  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标识',
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单日志表'
                """;
        jdbcTemplate.execute(sql);
        log.info("菜单日志表[p_sys_menu_log]初始化成功");
    }

    /**
     * 创建接口日志表
     */
    private void createApiLogTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS `p_sys_api_log` (
                  `id` bigint NOT NULL COMMENT '主键',
                  `user_id` bigint DEFAULT NULL COMMENT '请求用户ID',
                  `user_name` varchar(64) DEFAULT NULL COMMENT '请求用户名',
                  `nick_name` varchar(64) DEFAULT NULL COMMENT '请求用户昵称',
                  `request_url` varchar(512) DEFAULT NULL COMMENT '请求URL',
                  `request_method` varchar(16) DEFAULT NULL COMMENT '请求方法',
                  `request_time` datetime DEFAULT NULL COMMENT '请求时间',
                  `response_code` int DEFAULT NULL COMMENT '响应码',
                  `request_params` text DEFAULT NULL COMMENT '请求参数',
                  `response_result` text DEFAULT NULL COMMENT '返回值',
                  `cost_time` bigint DEFAULT NULL COMMENT '耗时(ms)',
                  `ip` varchar(64) DEFAULT NULL COMMENT '请求IP',
                  `description` varchar(256) DEFAULT NULL COMMENT '操作描述',
                  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
                  `create_id` bigint DEFAULT NULL COMMENT '创建人',
                  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                  `update_id` bigint DEFAULT NULL COMMENT '修改人',
                  `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                  `delete_flag` tinyint(1) DEFAULT 0 COMMENT '删除标识',
                  PRIMARY KEY (`id`)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='接口日志表'
                """;
        jdbcTemplate.execute(sql);
        log.info("接口日志表[p_sys_api_log]初始化成功");
    }
}

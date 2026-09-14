package com.art.constants;

/**
 * 租户相关常量
 *
 * @author Luminous.X
 * @since 2.0.0
 */
public class TenantConstants {
    /**
     * 租户ID 线程变量键
     */
    public static final String TENANT_ID = "tenantId";
    /**
     * 用户类型：超级管理员
     */
    public static final String USER_TYPE_SUPER_ADMIN = "superadmin";
    /**
     * 用户类型：租户管理员
     */
    public static final String USER_TYPE_ADMIN = "admin";
    /**
     * 用户类型：普通用户
     */
    public static final String USER_TYPE_NORMAL = "normal";

    /**
     * 登录会话租户上下文 Redis Key 前缀<br/>
     * 完整key: tenant_context:{token}，值为当前生效的租户ID（superadmin切换租户时更新）
     */
    public static final String TENANT_CONTEXT_KEY_PREFIX = "tenant_context:";

    private TenantConstants() {
    }
}
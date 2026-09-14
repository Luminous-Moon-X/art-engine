package com.art.utils;

import com.art.constants.TenantConstants;
import com.art.context.SecurityContextHolder;

import java.util.List;

/**
 * 当前用户信息工具类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SecurityUtil {
    /**
     * 获取当前登录用户id
     *
     * @return 当前登录用户id
     */
    public static Long getUserId() {
        return SecurityContextHolder.getUserId();
    }

    /**
     * 获取当前登录用户部门id
     *
     * @return 当前登录用户部门id
     */
    public static Long getDeptId() {
        return SecurityContextHolder.getDeptId();
    }

    /**
     * 获取当前登录用户角色id
     *
     * @return 当前登录用户角色id
     */
    public static List<Long> getRoleId() {
        return SecurityContextHolder.getRoleIds();
    }

    /**
     * 获取当前登录用户角色编码
     *
     * @return 当前登录用户角色编码
     */
    public static List<String> getRoleCodes() {
        return SecurityContextHolder.getRoleCodes();
    }

    /**
     * 获取当前登录用户名
     *
     * @return 当前登录用户名
     */
    public static String getUserName() {
        return SecurityContextHolder.getUserName();
    }

    /**
     * 获取当前登录用户名称
     *
     * @return 当前登录用户名称
     */
    public static String getUserAllName() {
        return SecurityContextHolder.getUserAllName();
    }

    /**
     * 获取当前登录用户类型
     *
     * @return 当前登录用户类型
     */
    public static String getUserType() {
        return SecurityContextHolder.getUserType();
    }

    /**
     * 当前用户是否为超级管理员
     *
     * @return 是否为超级管理员
     */
    public static Boolean isSuperAdmin() {
        return TenantConstants.USER_TYPE_SUPER_ADMIN.equals(getUserType());
    }

    /**
     * 当前用户是否为租户管理员
     *
     * @return 是否为租户管理员
     */
    public static Boolean isTenantAdmin() {
        return TenantConstants.USER_TYPE_ADMIN.equals(getUserType());
    }

    /**
     * 获取当前生效租户id
     *
     * @return 当前生效租户id
     */
    public static Long getTenantId() {
        return SecurityContextHolder.getTenantId();
    }


    /**
     * 获取当前登录用户时区配置
     *
     * @return 当前登录用户时区配置
     */
    public static String getTimezone() {
        return SecurityContextHolder.getTimezone();
    }

    /**
     * 获取当前登录用户ip
     *
     * @return 当前登录用户ip
     */
    public static String getLoginIp() {
        return SecurityContextHolder.getLoginIp();
    }

    /**
     * 获取当前登录Token密钥
     *
     * @return 当前登录Token密钥
     */
    public static String getToken() {
        return SecurityContextHolder.getToken();
    }
}

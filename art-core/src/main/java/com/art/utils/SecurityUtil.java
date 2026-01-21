package com.art.utils;

import com.art.context.SecurityContextHolder;

/**
 * 当前用户信息工具类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
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

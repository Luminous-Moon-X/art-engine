package com.art.log.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * User-Agent解析工具类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public class UserAgentUtil {

    /**
     * 获取浏览器信息
     *
     * @param request HTTP请求对象
     * @return 浏览器信息
     */
    public static String getBrowser(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "";
        }
        return parseBrowser(userAgent);
    }

    /**
     * 获取操作系统信息
     *
     * @param request HTTP请求对象
     * @return 操作系统信息
     */
    public static String getOs(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            return "";
        }
        return parseOs(userAgent);
    }

    /**
     * 解析浏览器类型
     */
    private static String parseBrowser(String userAgent) {
        if (userAgent.contains("Edg/")) {
            return "Edge";
        } else if (userAgent.contains("Chrome/") && !userAgent.contains("Edg/")) {
            return "Chrome";
        } else if (userAgent.contains("Firefox/")) {
            return "Firefox";
        } else if (userAgent.contains("Safari/") && !userAgent.contains("Chrome/")) {
            return "Safari";
        } else if (userAgent.contains("Opera") || userAgent.contains("OPR/")) {
            return "Opera";
        } else if (userAgent.contains("MSIE") || userAgent.contains("Trident/")) {
            return "IE";
        }
        return "Other";
    }

    /**
     * 解析操作系统类型
     */
    private static String parseOs(String userAgent) {
        if (userAgent.contains("Windows")) {
            return "Windows";
        } else if (userAgent.contains("Mac OS")) {
            return "Mac OS";
        } else if (userAgent.contains("Linux") && !userAgent.contains("Android")) {
            return "Linux";
        } else if (userAgent.contains("Android")) {
            return "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS";
        }
        return "Other";
    }
}

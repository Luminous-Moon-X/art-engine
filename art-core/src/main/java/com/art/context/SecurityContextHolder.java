package com.art.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.art.constants.SecurityConstants;
import com.art.utils.ConvertUtil;
import com.art.utils.StringUtil;
import io.micrometer.common.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 当前登陆人用户信息 线程变量
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SecurityContextHolder {
    /**
     * TTL线程变量<br/>
     * <p>
     * {@link TransmittableThreadLocal}
     */
    private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL = new TransmittableThreadLocal<>();

    /**
     * 设置线程变量值
     *
     * @param key   键
     * @param value 值
     */
    private static void set(String key, Object value) {
        Map<String, Object> map = getThreadLocalMap();
        map.put(key, value == null ? StringUtil.EMPTY : value);
    }

    /**
     * 获取线程变量值
     *
     * @param key 键
     * @return 线程变量值
     */
    private static String get(String key) {
        Map<String, Object> map = getThreadLocalMap();
        return ConvertUtil.toString(map.getOrDefault(key, StringUtil.EMPTY));
    }

    /**
     * 获取当前线程变量Map
     *
     * @return 线程变量上下文Map  {@link ConcurrentHashMap}
     */
    private static Map<String, Object> getThreadLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    /**
     * 设置角色id
     *
     * @param roleIds 角色id
     */
    public static void setRoleIds(List<Long> roleIds) {
        String roleIdsStr = roleIds.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        set(SecurityConstants.ROLE_IDS, roleIdsStr);
    }

    /**
     * 获取角色id
     *
     * @return 角色id
     */
    public static List<Long> getRoleIds() {
        List<Long> roleIds = new ArrayList<>();
        String roleIdsStr = get(SecurityConstants.ROLE_IDS);
        String[] roleIdStrArr = roleIdsStr.split(",");
        for (String roleIdStr : roleIdStrArr) {
            roleIds.add(Long.parseLong(roleIdStr));
        }
        return roleIds;
    }

    /**
     * 设置角色编码
     *
     * @param roleCodes 角色编码
     */
    public static void setRoleCodes(List<String> roleCodes) {
        String roleCodesStr = roleCodes.stream()
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        set(SecurityConstants.ROLE_CODES, roleCodesStr);
    }

    /**
     * 获取角色编码
     *
     * @return 角色编码
     */
    public static List<String> getRoleCodes() {
        String roleCodesStr = get(SecurityConstants.ROLE_CODES);
        String[] roleCodeStrArr = roleCodesStr.split(",");
        return new ArrayList<>(Arrays.asList(roleCodeStrArr));
    }

    /**
     * 设置部门ID
     *
     * @param deptId 部门id
     */
    public static void setDeptId(Long deptId) {
        set(SecurityConstants.DEPT_ID, deptId);
    }

    /**
     * 获取部门id
     *
     * @return 部门id
     */
    public static Long getDeptId() {
        return StringUtils.isBlank(get(SecurityConstants.DEPT_ID)) ? null : Long.parseLong(get(SecurityConstants.DEPT_ID));
    }

    /**
     * 设置用户id
     *
     * @param userId 用户id
     */
    public static void setUserId(Long userId) {
        set(SecurityConstants.USER_ID, userId);
    }

    /**
     * 获取用户id
     *
     * @return 用户id
     */
    public static Long getUserId() {
        return StringUtils.isBlank(get(SecurityConstants.USER_ID)) ? null : Long.parseLong(get(SecurityConstants.USER_ID));
    }

    /**
     * 设置用户名
     *
     * @param userName 用户名
     */
    public static void setUserName(String userName) {
        set(SecurityConstants.USER_NAME, userName);
    }

    /**
     * 获取用户名
     *
     * @return 用户名
     */
    public static String getUserName() {
        return get(SecurityConstants.USER_NAME);
    }

    /**
     * 设置用户名称
     *
     * @param userAllName 用户名称
     */
    public static void setUserAllName(String userAllName) {
        set(SecurityConstants.USER_ALL_NAME, userAllName);
    }

    /**
     * 获取用户名称
     *
     * @return 用户名称
     */
    public static String getUserAllName() {
        return get(SecurityConstants.USER_ALL_NAME);
    }

    /**
     * 设置用户类型
     *
     * @param userType 用户类型
     */
    public static void setUserType(String userType) {
        set(SecurityConstants.USER_TYPE, userType);
    }

    /**
     * 获取用户类型
     *
     * @return 用户类型
     */
    public static String getUserType() {
        return get(SecurityConstants.USER_TYPE);
    }

    /**
     * 设置当前生效租户id
     *
     * @param tenantId 租户id
     */
    public static void setTenantId(Long tenantId) {
        set(SecurityConstants.TENANT_ID, tenantId);
    }

    /**
     * 获取当前生效租户id
     *
     * @return 租户id
     */
    public static Long getTenantId() {
        return StringUtils.isBlank(get(SecurityConstants.TENANT_ID)) ? null : Long.parseLong(get(SecurityConstants.TENANT_ID));
    }

    /**
     * 设置时区
     *
     * @param timezone 时区
     */
    public static void setTimezone(String timezone) {
        set(SecurityConstants.TIMEZONE, timezone);
    }

    /**
     * 获取时区
     *
     * @return 时区
     */
    public static String getTimezone() {
        return get(SecurityConstants.TIMEZONE);
    }

    /**
     * 设置当前ip
     *
     * @param loginIp 当前ip
     */
    public static void setLoginIp(String loginIp) {
        set(SecurityConstants.LOGIN_IP, loginIp);
    }

    /**
     * 获取当前ip
     *
     * @return 当前ip
     */
    public static String getLoginIp() {
        return get(SecurityConstants.LOGIN_IP);
    }

    /**
     * 设置Token
     *
     * @param token token
     */
    public static void setToken(String token) {
        set(SecurityConstants.ACCESS_TOKEN, token);
    }

    /**
     * 获取Token
     *
     * @return token
     */
    public static String getToken() {
        return get(SecurityConstants.ACCESS_TOKEN);
    }

    /**
     * 清除用户线程变量
     */
    public static void clear() {
        THREAD_LOCAL.remove();
    }


}

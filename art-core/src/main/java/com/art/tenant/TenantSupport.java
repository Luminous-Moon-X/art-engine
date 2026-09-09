package com.art.tenant;

import com.art.config.TenantProperties;
import com.art.constants.TenantConstants;
import com.art.utils.StringUtil;
import com.mybatisflex.core.tenant.TenantManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 多租户业务支持类
 *
 * <p>用于区分【系统级数据】与【租户级数据】：系统级表（租户、租户套餐、菜单、规则、
 * 字典、认证缓存等）没有租户语义，其增删改查必须在 {@link #systemScope} 内执行，
 * 否则会因全局租户插件而误加 {@code tenant_id} 条件或被注入错误的租户值。
 * 角色（p_sys_role）为租户级数据，角色及权限关系的租户隔离由租户插件过滤与
 * {@link TenantSubjectValidator} 主体归属校验共同保证。</p>
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@Component
@SuppressWarnings("unused")
public class TenantSupport {

    /**
     * 多租户配置
     */
    private final TenantProperties tenantProperties;

    /**
     * 构造器注入
     *
     * @param tenantProperties 多租户配置
     */
    public TenantSupport(TenantProperties tenantProperties) {
        this.tenantProperties = tenantProperties;
    }

    /**
     * 多租户是否启用
     *
     * @return 是否启用
     */
    public boolean isEnable() {
        return Boolean.TRUE.equals(tenantProperties.getEnable());
    }

    /**
     * 获取默认租户ID（多租户关闭时登录使用的租户）
     *
     * @return 默认租户ID
     */
    public Long getDefaultTenantId() {
        return tenantProperties.getDefaultTenantId();
    }

    /**
     * 在系统级数据范围内执行操作：临时忽略租户条件，执行完成后自动恢复<br/>
     * 适用于对租户/租户套餐/菜单/规则/字典/角色/权限关系等系统级表的操作
     *
     * @param supplier 业务操作
     * @param <T>      返回类型
     * @return 业务操作结果
     */
    public <T> T systemScope(Supplier<T> supplier) {
        return TenantManager.withoutTenantCondition(supplier);
    }

    /**
     * 在系统级数据范围内执行操作（无返回值版本）
     *
     * @param runnable 业务操作
     */
    public void systemScope(Runnable runnable) {
        TenantManager.withoutTenantCondition(runnable);
    }

    /**
     * 是否为超级管理员
     *
     * @param userType 用户类型
     * @return 是否超级管理员
     */
    public boolean isSuperAdmin(String userType) {
        return TenantConstants.USER_TYPE_SUPER_ADMIN.equals(userType);
    }

    /**
     * 是否为租户管理员
     *
     * @param userType 用户类型
     * @return 是否租户管理员
     */
    public boolean isTenantAdmin(String userType) {
        return TenantConstants.USER_TYPE_ADMIN.equals(userType);
    }

    /**
     * 是否为平台级管理菜单的权限标识<br/>
     * 平台级管理菜单（菜单管理/字典管理/规则管理/租户管理/租户套餐管理等）操作的是系统级共享数据，
     * 不允许分配给租户套餐。匹配规则：
     * <ul>
     *     <li>标识等于配置前缀，或以"前缀:"开头；</li>
     *     <li>配置前缀为三段式（如 system:menu:list）时，同时拦截该模块下的按钮级标识
     *     （如 system:menu:add / system:menu:edit），避免按钮权限绕过平台菜单限制。</li>
     * </ul>
     *
     * @param sign 权限标识
     * @return 是否平台级管理菜单权限
     */
    public boolean isPlatformMenuSign(String sign) {
        if (StringUtil.isBlank(sign)) {
            return false;
        }
        for (String prefix : tenantProperties.getPlatformMenuSignPrefixes()) {
            if (StringUtil.isBlank(prefix)) {
                continue;
            }
            String trimmedPrefix = prefix.trim();
            if (sign.equals(trimmedPrefix) || sign.startsWith(trimmedPrefix + ":")) {
                return true;
            }
            // 三段式前缀（system:module:action）同时拦截该模块下的全部标识
            if (isThreeSegmentSign(trimmedPrefix)) {
                String modulePrefix = trimmedPrefix.substring(0, trimmedPrefix.lastIndexOf(':'));
                if (sign.equals(modulePrefix) || sign.startsWith(modulePrefix + ":")) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否为三段式权限标识（如 system:menu:list）
     *
     * @param sign 权限标识
     * @return 是否三段式
     */
    private boolean isThreeSegmentSign(String sign) {
        return sign.chars().filter(ch -> ch == ':').count() == 2;
    }

    /**
     * 从权限标识集合中剔除平台级管理菜单权限
     *
     * @param permissionSigns 权限标识集合
     * @return 过滤后的权限标识集合（入参为 null 时返回空集合）
     */
    public List<String> filterPlatformMenuSigns(List<String> permissionSigns) {
        if (permissionSigns == null || permissionSigns.isEmpty()) {
            return Collections.emptyList();
        }
        return permissionSigns.stream()
                .filter(sign -> !this.isPlatformMenuSign(sign))
                .toList();
    }

    /**
     * 将权限标识集合拼接为逗号分隔字符串
     *
     * @param permissionSigns 权限标识集合
     * @return 逗号分隔字符串（空则返回null）
     */
    public String joinSigns(List<String> permissionSigns) {
        if (permissionSigns == null || permissionSigns.isEmpty()) {
            return null;
        }
        return String.join(",", permissionSigns);
    }

    /**
     * 拆分逗号分隔的权限标识字符串
     *
     * @param signsStr 逗号分隔字符串
     * @return 权限标识集合
     */
    public List<String> splitSigns(String signsStr) {
        if (StringUtil.isBlank(signsStr)) {
            return Collections.emptyList();
        }
        return Arrays.stream(signsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
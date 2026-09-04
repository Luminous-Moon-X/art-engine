package com.art.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 多租户配置
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "art.tenant")
public class TenantProperties {
    /**
     * 是否启用多租户<br/>
     * 开启后：登录页展示租户下拉框，包含tenant_id列的表按当前租户自动过滤；
     * 关闭后：登录使用默认租户，不做租户数据过滤
     */
    private Boolean enable = false;
    /**
     * 默认租户ID（多租户关闭时登录使用的租户）
     */
    private Long defaultTenantId = 1L;
    /**
     * 平台级管理菜单的权限标识前缀<br/>
     * 菜单管理/字典管理/规则管理等平台级配置菜单（操作的是系统级共享数据）不允许分配给租户套餐：
     * 分配套餐的选择树会剔除、保存套餐时校验拦截。前缀匹配规则：标识等于前缀或以"前缀:"开头。
     * 需与数据库 p_sys_menu.permission_sign 的实际值保持一致，可按需增删。
     */
    private List<String> platformMenuSignPrefixes = new ArrayList<>(List.of(
            "system:menuManage",
            "system:dictManage",
            "system:ruleManage",
            "system:tenantManage",
            "system:tenantPackageManage"
    ));
}
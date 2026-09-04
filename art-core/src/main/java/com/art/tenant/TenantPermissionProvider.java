package com.art.tenant;

import java.util.List;

/**
 * 当前租户菜单范围提供者
 *
 * <p>由 art-system 模块的租户服务实现，供 art-core 的权限/菜单相关逻辑查询
 * 当前生效租户的套餐菜单范围（菜单、按钮的权限标识集合）。</p>
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@FunctionalInterface
public interface TenantPermissionProvider {

    /**
     * 获取当前生效租户可用的菜单权限标识集合<br/>
     * 空集合表示不限制（该租户可使用全部菜单）
     *
     * @return 菜单权限标识集合
     */
    List<String> getCurrentTenantPermissionSigns();
}
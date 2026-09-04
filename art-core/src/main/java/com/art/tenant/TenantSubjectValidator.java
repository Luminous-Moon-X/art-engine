package com.art.tenant;

/**
 * 授权主体租户归属校验
 *
 * <p>权限关系表（p_sys_role_permission / p_sys_user_permission / p_sys_dept_permission、
 * p_sys_user_role）无租户字段，按主体ID关联。为防止跨租户授权（如给别的租户的角色
 * 分配权限），写入前必须校验授权主体（角色/用户/部门）属于当前生效租户。</p>
 *
 * <p>由 art-system 模块的租户服务实现，供 art-core 的权限服务调用。</p>
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@FunctionalInterface
public interface TenantSubjectValidator {

    /**
     * 校验授权主体属于当前生效租户
     *
     * @param type 主体类型：role / user / dept
     * @param id   主体ID
     */
    void validateAssignable(String type, Long id);
}
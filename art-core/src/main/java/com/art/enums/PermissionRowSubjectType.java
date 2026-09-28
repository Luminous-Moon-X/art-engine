package com.art.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据行权限-授权主体类型
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Getter
@AllArgsConstructor
public enum PermissionRowSubjectType {
    ROLE("role", "角色"),
    DEPT("dept", "部门"),
    USER("user", "用户");
    private final String code;
    private final String name;
}

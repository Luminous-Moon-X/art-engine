package com.art.domain;

import com.art.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据行权限实体类
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PermissionRow extends BaseEntity {
    /**
     * 启用标识
     */
    private Boolean enableFlag;
    /**
     * 授权主体类型
     */
    private String subjectType;
    /**
     * 授权主体
     */
    private String permissionSubject;
    /**
     * 授权客体
     */
    private String permissionObject;
    /**
     * 授权范围
     */
    private String permissionScope;
    /**
     * 自定义部门权限范围
     */
    private String customDeptScope;
    /**
     * 自定义权限字段
     */
    private String columnCondition;
    /**
     * 自定义权限字段关系
     */
    private String columnRelation;
    /**
     * 自定义权限字段值
     */
    private String columnValue;
}

package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据行权限实体类
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Table("p_sys_permission_row")
@EqualsAndHashCode(callSuper = true)
@Data
public class PermissionRow extends BaseEntity {
    /**
     * 启用标识
     */
    @Column("enable_flag")
    private Boolean enableFlag;
    /**
     * 授权主体类型<br/>
     * <ul>
     *     <li>role:角色</li>
     *     <li>user:用户</li>
     *     <li>dept:部门</li>
     * </ul>
     */
    @Column("subject_type")
    private String subjectType;
    /**
     * 授权主体
     */
    @Column("permission_subject")
    private String permissionSubject;
    /**
     * 授权客体<br/>
     * <p>多选数据表，多个用逗号分隔</p>
     * 为空时，对所有数据表生效
     */
    @Column("permission_object")
    private String permissionObject;
    /**
     * 授权范围<br/>
     * <ul>
     *     <li>1：所属部门</li>
     *     <li>2：所属部门及以下</li>
     *     <li>3：本人创建数据</li>
     *     <li>4：自定义部门范围</li>
     *     <li>5：自定义字段</li>
     * </ul>
     */
    @Column("permission_scope")
    private String permissionScope;
    /**
     * 自定义部门权限范围
     */
    @Column("custom_dept_scope")
    private String customDeptScope;
    /**
     * 自定义权限字段
     */
    @Column("column_condition")
    private String columnCondition;
    /**
     * 自定义权限字段关系<br/>
     * <ul>
     *     <li>eq:等于</li>
     *     <li>ne:不等于</li>
     *     <li>like:包含</li>
     *     <li>not_like:不包含</li>
     *     <li>null:为空</li>
     *     <li>not_null:不为空</li>
     *     <li>gt:大于</li>
     *     <li>lt:小于</li>
     *     <li>ge:大于等于</li>
     *     <li>le:小于等于</li>
     * </ul>
     */
    @Column("column_relation")
    private String columnRelation;
    /**
     * 自定义权限字段值
     */
    @Column("column_value")
    private String columnValue;
}

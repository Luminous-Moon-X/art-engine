package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据行权限VO类
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Data
public class PermissionRowVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 启用标识
     */
    private Boolean enableFlag;
    /**
     * 授权主体类型<br/>
     * <ul>
     *     <li>role:角色</li>
     *     <li>user:用户</li>
     *     <li>dept:部门</li>
     * </ul>
     */
    private String subjectType;
    /**
     * 授权主体
     */
    private String permissionSubject;
    /**
     * 授权客体<br/>
     * <p>多选数据表，多个用逗号分隔；为空时，对所有数据表生效</p>
     */
    @Query(type = Query.Type.LIKE)
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
    private String permissionScope;
    /**
     * 自定义部门权限范围<br/>
     * <p>多选部门，多个用逗号分隔</p>
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

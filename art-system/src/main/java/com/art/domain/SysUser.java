package com.art.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表实体类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@Table("p_sys_user")
public class SysUser {
    /**
     * 用户表主键
     */
    @Id
    @Column("id")
    private Integer id;
    /**
     * 用户名
     */
    @Column("user_name")
    private String userName;
    /**
     * 用户名称
     */
    @Column("user_all_name")
    private String userAllName;
    /**
     * 用户类型
     */
    @Column("user_type")
    private String userType;
    /**
     * 所属组织
     */
    @Column("org_id")
    private String orgId;
    /**
     * 所属角色
     */
    @Column("role_id")
    private String roleId;
    /**
     * 时区
     */
    @Column("timezone")
    private String timezone;
    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;
    /**
     * 用户状态
     */
    @Column("user_status")
    private Integer userStatus;
    /**
     * 是否首次登录
     */
    @Column("first_login_flag")
    private Boolean firstLoginFlag;
    /**
     * 密码
     */
    @Column("password")
    private String password;
}

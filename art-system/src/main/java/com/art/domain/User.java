package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_sys_user")
public class User extends BaseEntity {
    /**
     * 启用标识
     */
    @Column("enable_flag")
    private Integer enableFlag;
    /**
     * 用户名
     */
    @Column("user_name")
    private String userName;
    /**
     * 昵称
     */
    @Column("nick_name")
    private String nickName;
    /**
     * 密码
     */
    @Column("password")
    private String password;
    /**
     * 用户类型
     */
    @Column("user_type")
    private String userType;
    /**
     * 部门ID
     */
    @Column("dept_id")
    private Long deptId;
    /**
     * 用户状态
     */
    @Column("user_status")
    private String userStatus;
    /**
     * 用户邮箱
     */
    @Column("user_email")
    private String userEmail;
    /**
     * 用户性别
     */
    @Column("user_gender")
    private String userGender;
    /**
     * 用户手机
     */
    @Column("user_phone")
    private String userPhone;
    /**
     * 用户地址
     */
    @Column("user_address")
    private String userAddress;
    /**
     * 用户描述
     */
    @Column("user_description")
    private String userDescription;
    /**
     * 用户标签
     */
    @Column("user_tag")
    private String userTag;
    /**
     * 是否首次登录
     */
    @Column("first_login_flag")
    private Boolean firstLoginFlag;
}

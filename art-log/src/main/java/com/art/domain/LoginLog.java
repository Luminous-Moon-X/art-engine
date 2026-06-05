package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_sys_login_log")
public class LoginLog extends BaseEntity {
    /**
     * 用户名
     */
    @Column("user_name")
    private String userName;
    /**
     * 用户昵称
     */
    @Column("nick_name")
    private String nickName;
    /**
     * 登录IP
     */
    @Column("login_ip")
    private String loginIp;
    /**
     * 登录时间
     */
    @Column("login_time")
    private LocalDateTime loginTime;
    /**
     * 浏览器
     */
    @Column("browser")
    private String browser;
    /**
     * 操作系统
     */
    @Column("os")
    private String os;
    /**
     * 登录状态(1成功 0失败)
     */
    @Column("status")
    private Integer status;
    /**
     * 提示信息
     */
    @Column("message")
    private String message;
}

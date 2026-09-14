package com.art.common;

import com.art.annotation.Sensitive;
import com.art.enums.SensitiveStrategy;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 当前登录用户信息
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Data
public class LoginUser {
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 启用标识
     */
    private Integer enableFlag;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 密码
     */
    @Sensitive(strategy = SensitiveStrategy.MASK_ALL)
    private String password;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 用户状态
     */
    private String userStatus;
    /**
     * 用户邮箱
     */
    @Sensitive(strategy = SensitiveStrategy.MASK_EMAIL)
    private String userEmail;
    /**
     * 用户性别
     */
    private String userGender;
    /**
     * 用户手机
     */
    @Sensitive(strategy = SensitiveStrategy.MASK_PHONE)
    private String userPhone;
    /**
     * 用户地址
     */
    private String userAddress;
    /**
     * 用户描述
     */
    private String userDescription;
    /**
     * 用户类型
     */
    private String userType;
    /**
     * 租户ID
     */
    private Long tenantId;
    /**
     * 用户标签
     */
    private String userTag;
    /**
     * 角色ID列表
     */
    private Long[] roleIds;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

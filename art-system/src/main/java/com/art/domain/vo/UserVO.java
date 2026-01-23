package com.art.domain.vo;

import lombok.Data;

/**
 * 用户VO类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class UserVO {
    /**
     * 用户ID
     */
    private Long id;
    /**
     * 启用标识
     */
    private Boolean enableFlag;
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
    private String userEmail;
    /**
     * 用户性别
     */
    private String userGender;
    /**
     * 用户手机
     */
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
     * 用户标签
     */
    private String userTag;
}

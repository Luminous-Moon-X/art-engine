package com.art.art.common;

import lombok.Data;

/**
 * 当前登录用户信息
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class LoginUser {
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户名称
     */
    private String userAllName;
    /**
     * 用户状态
     */
    private String userStatus;
    /**
     * 用户类型
     */
    private String userType;
    /**
     * 所属角色ID
     */
    private String[] roleIds;
    /**
     * 所属部门ID
     */
    private String[] deptIds;
}

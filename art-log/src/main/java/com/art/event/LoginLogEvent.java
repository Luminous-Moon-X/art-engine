package com.art.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

/**
 * 登录日志事件
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Getter
public class LoginLogEvent {

    /**
     * 用户名
     */
    private final String userName;
    /**
     * 用户昵称
     */
    private final String nickName;
    /**
     * HTTP请求对象
     */
    private final HttpServletRequest request;
    /**
     * 登录状态(1成功 0失败)
     */
    private final Integer status;
    /**
     * 提示信息
     */
    private final String message;
    /**
     * 生效租户ID（登录时选择的租户，用于日志归属）
     */
    private final Long tenantId;

    /**
     * 构造登录成功事件
     *
     * @param userName 用户名
     * @param nickName 用户昵称
     * @param request  HTTP请求对象
     */
    public LoginLogEvent(String userName, String nickName, HttpServletRequest request) {
        this(userName, nickName, request, null);
    }

    /**
     * 构造登录成功事件（含租户归属）
     *
     * @param userName 用户名
     * @param nickName 用户昵称
     * @param request  HTTP请求对象
     * @param tenantId 生效租户ID
     */
    public LoginLogEvent(String userName, String nickName, HttpServletRequest request, Long tenantId) {
        this.userName = userName;
        this.nickName = nickName;
        this.request = request;
        this.status = 1;
        this.message = "登录成功";
        this.tenantId = tenantId;
    }

    /**
     * 构造登录事件（含状态和消息）
     *
     * @param userName 用户名
     * @param nickName 用户昵称
     * @param request  HTTP请求对象
     * @param status   登录状态
     * @param message  提示信息
     */
    public LoginLogEvent(String userName, String nickName, HttpServletRequest request, Integer status, String message) {
        this.userName = userName;
        this.nickName = nickName;
        this.request = request;
        this.status = status;
        this.message = message;
        this.tenantId = null;
    }

}

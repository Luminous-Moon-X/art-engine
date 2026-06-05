package com.art.log.event;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 登录日志事件
 *
 * @author Luminous.X
 * @since 1.0.0
 */
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
     * 构造登录成功事件
     *
     * @param userName 用户名
     * @param nickName 用户昵称
     * @param request  HTTP请求对象
     */
    public LoginLogEvent(String userName, String nickName, HttpServletRequest request) {
        this.userName = userName;
        this.nickName = nickName;
        this.request = request;
        this.status = 1;
        this.message = "登录成功";
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
    }

    public String getUserName() {
        return userName;
    }

    public String getNickName() {
        return nickName;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

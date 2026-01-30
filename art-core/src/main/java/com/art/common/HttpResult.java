package com.art.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 统一请求返回值处理类
 *
 * @param <T> 返回数据类型
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@SuppressWarnings("unused")
public class HttpResult<T> implements Serializable {
    /**
     * 序列化版本号
     */
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 默认成功信息
     */
    private static final String DEFAULT_SUCCESS_MESSAGE = "操作成功！";
    /**
     * 默认失败信息
     */
    private static final String DEFAULT_FAILURE_MESSAGE = "操作失败！";

    /**
     * 状态码
     */
    private int code;
    /**
     * 返回信息
     */
    private String msg;
    /**
     * 返回数据<br/>
     * 字段为null时，自动忽略序列化该字段
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    /**
     * 根据状态码和返回信息构建返回对象
     *
     * @param code    状态码
     * @param message 返回信息
     */
    private HttpResult(int code, String message) {
        this.code = code;
        this.msg = message;
    }

    /**
     * 根据状态码、返回信息和返回数据构建返回对象
     *
     * @param code    状态码
     * @param message 返回信息
     * @param data    返回数据
     */
    private HttpResult(int code, String message, T data) {
        this.code = code;
        this.msg = message;
        this.data = data;
    }

    /**
     * 返回成功
     *
     * @return 成功统一返回值
     */
    public static <T> HttpResult<T> success() {
        return new HttpResult<>(200, DEFAULT_SUCCESS_MESSAGE);
    }

    /**
     * 返回成功
     *
     * @param message 成功信息
     * @return 成功统一返回值
     */
    public static <T> HttpResult<T> success(String message) {
        return new HttpResult<>(200, message);
    }

    /**
     * 返回成功
     *
     * @param data 返回对象
     * @param <T>  对象类型
     * @return 成功统一返回值
     */
    public static <T> HttpResult<T> success(T data) {
        return new HttpResult<>(200, DEFAULT_SUCCESS_MESSAGE, data);
    }

    /**
     * 返回成功
     *
     * @param message 成功信息
     * @param data    返回数据
     * @param <T>     返回数据类型
     * @return 成功统一返回值
     */
    public static <T> HttpResult<T> success(String message, T data) {
        return new HttpResult<>(200, message, data);
    }

    /**
     * 返回失败
     *
     * @return 失败统一返回值
     */
    public static <T> HttpResult<T> failure() {
        return new HttpResult<>(500, DEFAULT_FAILURE_MESSAGE);
    }

    /**
     * 返回失败
     *
     * @param message 失败信息
     * @return 失败统一返回值
     */
    public static <T> HttpResult<T> failure(String message) {
        return new HttpResult<>(500, message);
    }

    /**
     * 返回失败
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @return 失败统一返回值
     */
    public static <T> HttpResult<T> failure(int errorCode, String message) {
        return new HttpResult<>(errorCode, message);
    }

    /**
     * 返回失败
     *
     * @param message 错误信息
     * @param data    返回数据
     * @param <T>     返回数据类型
     * @return 失败统一返回值
     */
    public static <T> HttpResult<T> failure(String message, T data) {
        return new HttpResult<>(500, message, data);
    }

    /**
     * 返回失败
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param data      返回数据
     * @param <T>       返回数据类型
     * @return 失败统一返回值
     */
    public static <T> HttpResult<T> failure(int errorCode, String message, T data) {
        return new HttpResult<>(errorCode, message, data);
    }
}

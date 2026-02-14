package com.art.common;

import org.springframework.web.bind.annotation.RestController;

/**
 * 控制器基类
 * <p>
 * 封装常用的HttpResult返回方法，提供便捷的响应处理能力
 *
 * @author Luminous.X
 * @since 1.1.2
 */
@RestController
public class BaseController {

    /**
     * 返回成功结果
     *
     * @param <T> 返回数据类型
     * @return 成功统一返回值
     */
    protected <T> HttpResult<T> success() {
        return HttpResult.success();
    }

    /**
     * 返回成功结果
     *
     * @param message 成功信息
     * @param <T>     返回数据类型
     * @return 成功统一返回值
     */
    protected <T> HttpResult<T> success(String message) {
        return HttpResult.success(message);
    }

    /**
     * 返回成功结果
     *
     * @param data 返回数据
     * @param <T>  返回数据类型
     * @return 成功统一返回值
     */
    protected <T> HttpResult<T> success(T data) {
        return HttpResult.success(data);
    }

    /**
     * 返回成功结果
     *
     * @param message 成功信息
     * @param data    返回数据
     * @param <T>     返回数据类型
     * @return 成功统一返回值
     */
    protected <T> HttpResult<T> success(String message, T data) {
        return HttpResult.success(message, data);
    }

    /**
     * 返回失败结果
     *
     * @param <T> 返回数据类型
     * @return 失败统一返回值
     */
    protected <T> HttpResult<T> failure() {
        return HttpResult.failure();
    }

    /**
     * 返回失败结果
     *
     * @param message 失败信息
     * @param <T>     返回数据类型
     * @return 失败统一返回值
     */
    protected <T> HttpResult<T> failure(String message) {
        return HttpResult.failure(message);
    }

    /**
     * 返回失败结果
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param <T>       返回数据类型
     * @return 失败统一返回值
     */
    protected <T> HttpResult<T> failure(int errorCode, String message) {
        return HttpResult.failure(errorCode, message);
    }

    /**
     * 返回失败结果
     *
     * @param message 失败信息
     * @param data    返回数据
     * @param <T>     返回数据类型
     * @return 失败统一返回值
     */
    protected <T> HttpResult<T> failure(String message, T data) {
        return HttpResult.failure(message, data);
    }

    /**
     * 返回失败结果
     *
     * @param errorCode 错误码
     * @param message   错误信息
     * @param data      返回数据
     * @param <T>       返回数据类型
     * @return 失败统一返回值
     */
    protected <T> HttpResult<T> failure(int errorCode, String message, T data) {
        return HttpResult.failure(errorCode, message, data);
    }
}
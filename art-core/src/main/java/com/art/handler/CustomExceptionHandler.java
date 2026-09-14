package com.art.handler;

import cn.dev33.satoken.exception.NotPermissionException;
import com.art.exception.ArtException;
import com.art.common.HttpResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 自定义异常拦截处理类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestControllerAdvice
public class CustomExceptionHandler {
    /**
     * 日志
     */
    private static final Logger log = LoggerFactory.getLogger(CustomExceptionHandler.class);

    /**
     * Art平台异常拦截
     *
     * @param exception 异常类
     * @return 拦截处理返回值
     */
    @ExceptionHandler({ArtException.class})
    public HttpResult<String> handleArtException(ArtException exception) {
        log.error("Throws an Art Exception ---> ", exception);
        return HttpResult.failure(exception.getErrorCode(), exception.getErrorMessage());
    }

    /**
     * 接口无权限异常拦截
     *
     * @param exception 异常类
     * @return 拦截处理返回值
     */
    @ExceptionHandler({NotPermissionException.class})
    public HttpResult<String> handleNotPermissionException(NotPermissionException exception) {
        log.error("Throws an NotPermissionException ---> ", exception);
        return HttpResult.failure(403, "当前用户无该接口权限！");
    }

    /**
     * 未知异常兜底拦截
     *
     * <p>防止 systemScope（系统级作用域）及业务调用链中抛出的非业务异常
     * （SQL 异常、空指针、事务包装异常等）无人打印日志，导致问题静默难排查。</p>
     *
     * @param exception 异常类
     * @return 拦截处理返回值
     */
    @ExceptionHandler(Exception.class)
    public HttpResult<String> handleException(Exception exception) {
        log.error("Throws an unhandled Exception ---> ", exception);
        return HttpResult.failure(500, "系统异常，请联系管理员！");
    }

}

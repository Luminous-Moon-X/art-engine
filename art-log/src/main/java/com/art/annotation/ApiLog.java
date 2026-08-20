package com.art.annotation;

import java.lang.annotation.*;

import com.art.enums.ApiOperationType;

/**
 * 接口日志注解
 * <p>
 * 加在Controller接口方法上，请求该接口后自动记录用户请求行为
 *
 * @author Luminous.X
 * @since 1.2.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {
    /**
     * 功能模块名称
     *
     * @return 功能模块名称
     */
    String module();

    /**
     * 操作类型
     *
     * @return 操作类型
     */
    ApiOperationType operationType() default ApiOperationType.QUERY;

    /**
     * 操作描述
     *
     * @return 操作描述
     */
    String description() default "";

    /**
     * 需要排除的请求参数字段，支持点路径，如 user.password
     */
    String[] excludeParams() default {};

    /**
     * 需要排除的响应字段，支持点路径，如 data.token
     */
    String[] excludeResponse() default {};
}

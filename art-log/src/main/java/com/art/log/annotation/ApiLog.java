package com.art.log.annotation;

import java.lang.annotation.*;

/**
 * 接口日志注解
 * <p>
 * 加在Controller接口方法上，请求该接口后自动记录用户请求行为
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiLog {
    /**
     * 操作描述
     */
    String value() default "";
}

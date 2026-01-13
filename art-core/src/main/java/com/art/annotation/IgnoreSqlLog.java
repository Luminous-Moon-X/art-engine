package com.art.annotation;

import java.lang.annotation.*;

/**
 * 忽略sql日志<br/>
 * 在对应方法上加上该注解，则屏蔽该方法的SQL日志打印
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreSqlLog {

}

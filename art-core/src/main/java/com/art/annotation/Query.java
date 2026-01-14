package com.art.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 查询条件注解
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {
    // 匹配方式，默认等于
    Type type() default Type.EQ;

    // 数据库列名，如果不填则自动转换驼峰为下划线
    String column() default "";

    enum Type {
        EQ, LIKE, GT, LT, GE, LE, BETWEEN
    }
}

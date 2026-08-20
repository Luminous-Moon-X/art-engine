package com.art.annotation;

import com.art.enums.SensitiveStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字段脱敏标注
 *
 * @author Luminous.X
 * @since 1.3.3
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
    /**
     * 脱敏策略
     */
    SensitiveStrategy strategy() default SensitiveStrategy.MASK_ALL;

    /**
     * 保留前缀长度，通用策略使用
     */
    int prefixLen() default 0;

    /**
     * 保留后缀长度，通用策略使用
     */
    int suffixLen() default 0;

    /**
     * 掩码字符
     */
    String maskChar() default "*";
}

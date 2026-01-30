package com.art.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;

/**
 * Spring工具类
 *
 * @author Lumonous.X
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class SpringUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    /**
     * 根据类型获取Bean
     *
     * @param beanClass bean类型
     * @param <T>       bean类型
     * @return Bean实例
     */
    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }

    /**
     * 根据Bean名称获取指定类型的Bean实例
     *
     * @param name      Bean名称
     * @param beanClass Bean类型
     * @param <T>       Bean类型
     * @return Bean实例
     */
    public static <T> T getBean(String name, Class<T> beanClass) {
        return context.getBean(name, beanClass);
    }

    /**
     * 根据Bean名称获取Bean实例
     *
     * @param name Bean名称
     * @return Bean实例 {@link Object}
     */
    public static Object getBean(String name) {
        return context.getBean(name);
    }
}

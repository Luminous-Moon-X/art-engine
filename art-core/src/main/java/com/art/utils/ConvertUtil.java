package com.art.utils;

import cn.hutool.core.convert.Convert;

import java.util.Collections;
import java.util.List;

/**
 * 类型转换工具类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class ConvertUtil {
    /**
     * 转换为字符串，无默认值
     *
     * @return 目标字符串
     */
    public static String toString(Object origin) {
        return toString(origin, null);
    }

    /**
     * 转换为字符串，指定默认值
     *
     * @return 目标字符串
     */
    public static String toString(Object origin, String defaultValue) {
        if (origin == null) {
            return defaultValue;
        }
        if (origin instanceof String target) {
            return target;
        }
        return origin.toString();
    }

    /**
     * 转换集合类型
     *
     * @param originList  源集合
     * @param targetClass 转换目标类型Class
     * @param <T>         目标类型
     * @return 转换类型后的集合
     */
    public static <T> List<T> convertList(List<?> originList, Class<T> targetClass) {
        List<T> targetList = new java.util.ArrayList<>(Collections.emptyList());
        for (Object o : originList) {
            T target = Convert.convert(targetClass, o);
            targetList.add(target);
        }
        return targetList;
    }

    /**
     * 对象类型转换
     *
     * @param origin      源对象
     * @param targetClass 目标类型
     * @param <T>         目标类型
     * @return 转换后类型对象
     */
    public static <T> T convert(Object origin, Class<T> targetClass) {
        return Convert.convert(targetClass, origin);
    }
}

package com.art.context;

/**
 * 忽略SQL日志上下文
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
public class IgnoreSqlLogContextHolder {
    /**
     * 忽略SQL日志上下文
     */
    private static final ThreadLocal<Boolean> IGNORE_SQL_LOG = new ThreadLocal<>();

    /**
     * 启用忽略SQL日志
     */
    public static void enable() {
        IGNORE_SQL_LOG.set(true);
    }

    /**
     * 禁用忽略SQL日志
     */
    public static void disable() {
        IGNORE_SQL_LOG.set(false);
    }

    /**
     * 是否忽略SQL日志打印
     *
     * @return 是否忽略SQL日志打印
     */
    public static Boolean ignore() {
        return IGNORE_SQL_LOG.get();
    }

}

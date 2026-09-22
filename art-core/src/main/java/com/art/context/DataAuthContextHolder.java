package com.art.context;

import java.util.function.Supplier;

/**
 * 数据行权限忽略上下文
 *
 * <p>数据行权限方言（{@code DataAuthDialect}）在构建 SQL 时会读取数据行权限缓存，
 * 而该缓存自身的加载 SQL 同样会经过方言。为避免加载权限规则时递归触发权限过滤
 * （缓存尚未写入 → 再次加载 → 无限递归），加载期间必须通过本上下文临时关闭
 * 数据行权限处理。</p>
 *
 * @author Luminous.X
 * @since 2.1.0
 */
public class DataAuthContextHolder {
    /**
     * 忽略数据行权限上下文
     */
    private static final ThreadLocal<Boolean> IGNORE_DATA_AUTH = new ThreadLocal<>();

    /**
     * 启用忽略数据行权限
     */
    public static void enable() {
        IGNORE_DATA_AUTH.set(true);
    }

    /**
     * 禁用忽略数据行权限
     */
    public static void disable() {
        IGNORE_DATA_AUTH.remove();
    }

    /**
     * 是否忽略数据行权限处理
     *
     * @return 是否忽略数据行权限处理
     */
    public static boolean ignore() {
        return Boolean.TRUE.equals(IGNORE_DATA_AUTH.get());
    }

    /**
     * 在忽略数据行权限的上下文中执行操作，执行完成后自动恢复原有状态
     *
     * @param supplier 业务操作
     * @param <T>      返回类型
     * @return 业务操作结果
     */
    public static <T> T ignoreScope(Supplier<T> supplier) {
        boolean origin = ignore();
        enable();
        try {
            return supplier.get();
        } finally {
            if (origin) {
                enable();
            } else {
                disable();
            }
        }
    }
}

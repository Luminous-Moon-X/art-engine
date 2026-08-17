package com.art.cache.support;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 二级缓存注册表：维护缓存Key与缓存实例的映射，供失效广播与统一刷新定位缓存
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Component
public class ArtCacheRegistry {
    /**
     * 缓存Key → 缓存实例
     */
    private final Map<String, ArtCache<?>> caches = new ConcurrentHashMap<>();

    /**
     * 注册缓存实例（由 ArtCacheRunner 启动时调用）
     *
     * @param cache 缓存实例
     */
    public void register(ArtCache<?> cache) {
        ArtCache<?> exist = caches.putIfAbsent(cache.redisKey(), cache);
        if (exist != null && exist != cache) {
            throw new IllegalStateException("缓存Key重复注册：" + cache.redisKey()
                    + "（已存在 " + exist.getClass().getName() + "，冲突 " + cache.getClass().getName() + "）");
        }
    }

    /**
     * 根据缓存Key查找缓存实例
     *
     * @param key 缓存Key
     * @return 缓存实例，不存在返回 null
     */
    public ArtCache<?> findByKey(String key) {
        return caches.get(key);
    }
}

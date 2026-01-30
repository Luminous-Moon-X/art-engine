package com.art;

import com.art.context.IgnoreSqlLogContextHolder;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 缓存自动初始化模板类，用来自动初始化并预热缓存数据<br/>
 * 使用方法：继承该类，并实现抽象方法，即可自动初始化并预热缓存数据
 *
 * @param <String> RedisKey
 * @param <V>      缓存数据
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
public abstract class ArtCache<String, V> {
    /**
     * Redis客户端
     */
    protected final RedisTemplate<String, Object> redisTemplate;
    /**
     * Caffeine缓存
     */
    protected final Cache<String, V> caffeineCache;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     */
    public ArtCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.caffeineCache = Caffeine.newBuilder()
                .maximumSize(10000)
                .build();
    }

    /**
     * 缓存名称，由子类实现<br/>
     * 例如：菜单、数据字典、权限标识……
     *
     * @return 缓存名称
     */
    protected abstract String getCacheName();

    /**
     * 缓存Key，由子类实现<br/>
     * 例如：menu、dict、permission……
     *
     * @return 缓存Key
     */
    protected abstract String getRedisKey();

    /**
     * 获取缓存数据，由子类实现
     *
     * @return 缓存数据
     */
    protected abstract V getCacheData();

    /**
     * 自动初始化缓存并预热
     */
    public void init() {
        IgnoreSqlLogContextHolder.enable();
        V data = getCacheData();
        IgnoreSqlLogContextHolder.disable();
        if (data != null) {
            redisTemplate.opsForValue().set(getRedisKey(), data);
            caffeineCache.put(getRedisKey(), data);
            log.info("加载二级缓存成功：{}", getCacheName());
        }
    }

    /**
     * 缓存数据
     *
     * @param val 数据
     */
    public void cache(V val) {
        if (val != null) {
            redisTemplate.opsForValue().set(getRedisKey(), val);
            caffeineCache.put(getRedisKey(), val);
        }
    }

    /**
     * 提供统一的获取缓存数据的方法
     *
     * @return 缓存数据
     */
    public V get() {
        String key = getRedisKey();
        // 先查本地缓存
        V value = caffeineCache.getIfPresent(key);
        if (value != null) return value;

        // 再查 Redis
        value = (V) redisTemplate.opsForValue().get(key.toString());
        if (value != null) {
            caffeineCache.put(key, value);
            return value;
        }

        // 兜底逻辑：重新从 DB 加载
        V cacheData = getCacheData();
        cache(cacheData);
        return cacheData;
    }
}

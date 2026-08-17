package com.art.cache.support;

import com.art.context.IgnoreSqlLogContextHolder;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 二级缓存模板类（L1 Caffeine + L2 Redis），只负责缓存机制，不携带业务数据加载逻辑<br/>
 * 使用方法：继承该类并实现 {@link #cacheName()}、{@link #redisKey()}、{@link #loadFromDb()}，即可自动注册并预热缓存数据
 * <p>
 * 生命周期语义（模块边界约定）：
 * <ul>
 *     <li>{@link #warmUp()}：启动预热，仅允许 {@link ArtCacheRunner} 调用</li>
 *     <li>{@link #reload()}：业务写操作提交后的全量刷新（重载 DB → L2 → L1），业务代码通过
 *     {@link CacheRefreshService#refreshAfterCommit(ArtCache)} 触发，禁止直接调用</li>
 *     <li>{@link #evict()}：缓存失效（删除 L2 + 清空本地 L1），业务代码通过
 *     {@link CacheRefreshService#evictAfterCommit(ArtCache)} 触发，禁止直接调用</li>
 *     <li>{@link #clearLocal()}：仅清除本地 L1，供接收其他实例的失效广播时使用</li>
 * </ul>
 *
 * @param <V> 缓存数据类型
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
public abstract class ArtCache<V> {

    /**
     * Redis客户端
     */
    protected final RedisTemplate<String, Object> redisTemplate;
    /**
     * Caffeine缓存（L1）
     */
    protected final Cache<String, V> caffeineCache;
    /**
     * 缓存配置
     */
    private final ArtCacheProperties properties;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param properties    缓存配置
     */
    public ArtCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        Caffeine<Object, Object> builder = Caffeine.newBuilder()
                .maximumSize(properties.getL1MaxSize());
        if (properties.getL1TtlSeconds() > 0) {
            builder.expireAfterWrite(properties.getL1TtlSeconds(), TimeUnit.SECONDS);
        }
        // build() 支持目标类型推导，得到 Cache<String, V>
        this.caffeineCache = builder.build();
    }

    /**
     * 缓存名称，由子类实现<br/>
     * 例如：菜单、数据字典、权限标识……
     *
     * @return 缓存名称
     */
    public abstract String cacheName();

    /**
     * 缓存Key（L1/L2 共用），由子类实现，需全局唯一<br/>
     * 例如：menuTree、dict、menuPermission:role……
     *
     * @return 缓存Key
     */
    public abstract String redisKey();

    /**
     * 从数据库加载缓存数据，由数据所属模块的缓存实现类提供
     *
     * @return 缓存数据
     */
    protected abstract V loadFromDb();

    /**
     * 启动预热：加载 DB 数据并写入 L2/L1，仅允许 {@link ArtCacheRunner} 调用
     */
    public void warmUp() {
        V data = load();
        if (data != null) {
            writeBoth(data);
            log.info("缓存预热成功：{}", cacheName());
        }
    }

    /**
     * 全量刷新：重载 DB 数据并写入 L2/L1<br/>
     * 业务代码禁止直接调用，请使用 {@link CacheRefreshService#refreshAfterCommit(ArtCache)}
     */
    public void reload() {
        V data = load();
        if (data != null) {
            writeBoth(data);
            log.info("缓存刷新成功：{}", cacheName());
        }
    }

    /**
     * 缓存失效：删除 L2 并清空本地 L1<br/>
     * 业务代码禁止直接调用，请使用 {@link CacheRefreshService#evictAfterCommit(ArtCache)}
     */
    public void evict() {
        redisTemplate.delete(redisKey());
        clearLocal();
        log.info("缓存已失效：{}", cacheName());
    }

    /**
     * 仅清除本地 L1（供接收其他实例的失效广播时使用）
     */
    public void clearLocal() {
        caffeineCache.invalidate(redisKey());
    }

    /**
     * 提供统一的获取缓存数据的方法：L1 → L2 → DB（兜底重载）
     *
     * @return 缓存数据
     */
    @SuppressWarnings("unchecked")
    public V get() {
        String key = redisKey();
        // 先查本地缓存
        V value = caffeineCache.getIfPresent(key);
        if (value != null) {
            return value;
        }
        // 再查 Redis
        value = (V) redisTemplate.opsForValue().get(key);
        if (value != null) {
            caffeineCache.put(key, value);
            return value;
        }
        // 兜底逻辑：重新从 DB 加载
        V data = load();
        if (data != null) {
            writeBoth(data);
        }
        return data;
    }

    /**
     * 从 DB 加载数据（加载期间抑制 SQL 日志）
     *
     * @return 缓存数据
     */
    private V load() {
        IgnoreSqlLogContextHolder.enable();
        try {
            return loadFromDb();
        } finally {
            IgnoreSqlLogContextHolder.disable();
        }
    }

    /**
     * 同时写入 L2（Redis）与 L1（Caffeine）
     *
     * @param data 缓存数据
     */
    private void writeBoth(V data) {
        if (properties.getL2TtlSeconds() > 0) {
            redisTemplate.opsForValue().set(redisKey(), data, properties.getL2TtlSeconds(), TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(redisKey(), data);
        }
        caffeineCache.put(redisKey(), data);
    }
}

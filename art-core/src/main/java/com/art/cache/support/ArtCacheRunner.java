package com.art.cache.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 缓存预热器：收集所有 ArtCache Bean，注册到缓存注册表并逐个预热（SmartInitializingSingleton）
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
@Component
public class ArtCacheRunner implements SmartInitializingSingleton {
    /**
     * 所有 ArtCache Bean
     */
    private final List<ArtCache<?>> artCaches;
    /**
     * 缓存注册表
     */
    private final ArtCacheRegistry registry;
    /**
     * 失效广播器
     */
    private final CacheInvalidationBroadcaster broadcaster;

    /**
     * 构造函数
     *
     * @param artCaches   所有 ArtCache Bean
     * @param registry    缓存注册表
     * @param broadcaster 失效广播器
     */
    public ArtCacheRunner(List<ArtCache<?>> artCaches, ArtCacheRegistry registry, CacheInvalidationBroadcaster broadcaster) {
        this.artCaches = artCaches;
        this.registry = registry;
        this.broadcaster = broadcaster;
    }

    /**
     * 缓存注册与预热
     */
    @Override
    public void afterSingletonsInstantiated() {
        log.info("--- 开始预热二级缓存（共 {} 个） ---", artCaches.size());
        for (ArtCache<?> cache : artCaches) {
            try {
                registry.register(cache);
                cache.warmUp();
            } catch (Exception e) {
                log.error("缓存预热失败：{}", cache.getClass().getName(), e);
            }
        }
        broadcaster.start();
        log.info("--- 二级缓存预热完成 ---");
    }
}

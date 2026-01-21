package com.art;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@DependsOn("redisTemplate")
public class ArtCacheRunner implements SmartInitializingSingleton {
    /**
     * 所有 ArtCache Bean
     */
    private final List<ArtCache<?, ?>> artCaches;

    /**
     * 构造函数
     *
     * @param artCaches 所有 ArtCache Bean
     */
    public ArtCacheRunner(List<ArtCache<?, ?>> artCaches) {
        this.artCaches = artCaches;
    }

    /**
     * 缓存预热
     */
    @Override
    public void afterSingletonsInstantiated() {
        System.out.println("--- 开始预热二级缓存 ---");
        for (ArtCache<?, ?> cache : artCaches) {
            try {
                cache.init();
            } catch (Exception e) {
                System.err.println("缓存初始化失败: " + cache.getClass().getName());
                log.error("An error has occurred: ", e);
            }
        }
        System.out.println("--- 二级缓存预热完成 ---");
    }
}

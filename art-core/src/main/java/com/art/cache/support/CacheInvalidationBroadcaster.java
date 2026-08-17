package com.art.cache.support;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 二级缓存失效广播器：基于 Redisson RTopic 实现跨实例的 L1 缓存失效通知<br/>
 * 广播消息为缓存Key字符串，接收方仅清除本地 L1，下次读取时通过 L2 或 DB 重新加载，避免多实例间缓存不一致
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Slf4j
@Component
public class CacheInvalidationBroadcaster {
    /**
     * 失效广播主题
     */
    public static final String TOPIC = "art:cache:invalidation";

    /**
     * Redisson客户端
     */
    private final RedissonClient redissonClient;
    /**
     * 缓存注册表
     */
    private final ArtCacheRegistry registry;
    /**
     * 广播主题
     */
    private RTopic topic;

    /**
     * 构造函数
     *
     * @param redissonClient Redisson客户端
     * @param registry       缓存注册表
     */
    public CacheInvalidationBroadcaster(RedissonClient redissonClient, ArtCacheRegistry registry) {
        this.redissonClient = redissonClient;
        this.registry = registry;
    }

    /**
     * 订阅失效消息（由 ArtCacheRunner 在缓存注册完成后调用）
     */
    public void start() {
        this.topic = redissonClient.getTopic(TOPIC);
        this.topic.addListener(String.class, (channel, key) -> {
            ArtCache<?> cache = registry.findByKey(key);
            if (cache != null) {
                cache.clearLocal();
                log.info("收到缓存失效广播，已清除本地缓存：{}", key);
            } else {
                log.warn("收到未知缓存的失效广播，已忽略：{}", key);
            }
        });
        log.info("二级缓存失效广播订阅成功：{}", TOPIC);
    }

    /**
     * 广播缓存失效消息
     *
     * @param key 缓存Key
     */
    public void broadcast(String key) {
        if (topic == null) {
            log.warn("失效广播尚未启动，忽略消息：{}", key);
            return;
        }
        topic.publish(key);
    }
}

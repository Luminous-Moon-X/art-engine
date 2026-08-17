package com.art.cache.support;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 二级缓存配置项（前缀 art.cache）
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "art.cache")
public class ArtCacheProperties {
    /**
     * L1 Caffeine 最大条目数
     */
    private int l1MaxSize = 10000;
    /**
     * L1 Caffeine 写入后过期时间（秒），<=0 表示永不过期
     */
    private long l1TtlSeconds = 3600;
    /**
     * L2 Redis 过期时间（秒），<=0 表示永不过期
     */
    private long l2TtlSeconds = 7200;
}

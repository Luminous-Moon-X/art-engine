package com.art.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis 配置类<br/>
 * 用于配置 Redis 连接工厂和 Redis 模板
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.password}")
    private String password;

    @Value("${spring.data.redis.database}")
    private int database;

    /**
     * 配置 Redis 连接工厂
     * 使用 Lettuce 客户端连接 Redis，配置了连接参数和客户端选项
     *
     * @return LettuceConnectionFactory Redis 连接工厂实例
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        // 创建 Redis 独立配置对象
        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
        serverConfig.setHostName(host);
        serverConfig.setPort(port);
        serverConfig.setPassword(password);
        serverConfig.setDatabase(database);

        // 配置 Socket 选项
        SocketOptions socketOptions = SocketOptions.builder()
                .keepAlive(true)
                .tcpNoDelay(true)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // 配置客户端选项
        ClientOptions clientOptions = ClientOptions.builder()
                .socketOptions(socketOptions)
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .publishOnScheduler(true)
                .build();

        // 配置 Lettuce 客户端配置
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(10))
                .shutdownTimeout(Duration.ofMillis(200))
                .clientOptions(clientOptions)
                .build();

        // 创建连接工厂实例
        LettuceConnectionFactory factory = new LettuceConnectionFactory(serverConfig, clientConfig);
        factory.setValidateConnection(true); // 验证连接
        return factory;
    }

    /**
     * 配置 Redis 模板<br/>
     * 设置键值序列化方式，使用字符串作为键，JSON 作为值的序列化方式
     *
     * @param connectionFactory Redis 连接工厂
     * @return RedisTemplate<String, Object> Redis 模板实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}

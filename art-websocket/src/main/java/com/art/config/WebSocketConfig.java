package com.art.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单内存消息代理，用于向客户端推送消息
        registry.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息的前缀，如 /app/sendMessage
        registry.setApplicationDestinationPrefixes("/app");
        // 可选：设置用户目标前缀（用于点对点）
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 STOMP 协议的端点，前端将连接到这里
        registry.addEndpoint("/ws-endpoint") // WebSocket 连接端点
                .setAllowedOriginPatterns("*") // 允许跨域访问
                .withSockJS(); // 兼容降级为 WebSocket 的 SockJS 协议
    }
}

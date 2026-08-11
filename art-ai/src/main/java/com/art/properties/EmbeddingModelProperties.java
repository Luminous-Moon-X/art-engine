package com.art.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 嵌入模型配置
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Getter
@Setter
@NoArgsConstructor
@Component("embeddingModelProperties")
@ConfigurationProperties(prefix = "art.embedding-model")
public class EmbeddingModelProperties {
    /**
     * 模型提供者
     *
     * <ul>
     *     <li>openAi</li>
     *     <li>ollama</li>
     * </ul>
     */
    private String provider;
    /**
     * 模型地址端口
     */
    private String endpoint;
    /**
     * 模型请求路径
     */
    private String requestPath;
    /**
     * 模型名称
     */
    private String model;
    /**
     * api-key
     */
    private String apiKey;
}

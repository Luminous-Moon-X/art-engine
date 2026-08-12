package com.art.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 重排模型配置
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Getter
@Setter
@NoArgsConstructor
@Component("rerankModelProperties")
@ConfigurationProperties(prefix = "art.rerank-model")
public class RerankModelProperties {
    /**
     * 模型地址端口
     */
    private String endpoint;
    /**
     * 请求地址
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
    /**
     * 是否启用
     */
    private Boolean enabled;
}

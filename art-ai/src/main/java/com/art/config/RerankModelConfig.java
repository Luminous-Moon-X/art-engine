package com.art.config;

import com.agentsflex.rerank.DefaultRerankModel;
import com.agentsflex.rerank.DefaultRerankModelConfig;
import com.art.properties.RerankModelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * 初始化重排模型实例
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Slf4j
@Component
@Configuration
@DependsOn("rerankModelProperties")
@ConditionalOnProperty(name = "art.rerank-model.enabled", havingValue = "true")
public class RerankModelConfig {
    /**
     * 重排模型配置
     */
    private final RerankModelProperties rerankModelProperties;

    /**
     * 构造函数
     *
     * @param rerankModelProperties 重排模型配置
     */
    public RerankModelConfig(RerankModelProperties rerankModelProperties) {
        this.rerankModelProperties = rerankModelProperties;
    }

    /**
     * 重排模型实例
     *
     * @return {@link DefaultRerankModel} 重排模型实例
     */
    @Bean
    public DefaultRerankModel rerankModel() {
        DefaultRerankModelConfig config = new DefaultRerankModelConfig();
        config.setEndpoint(rerankModelProperties.getEndpoint());
        config.setRequestPath(rerankModelProperties.getRequestPath());
        config.setModel(rerankModelProperties.getModel());
        config.setApiKey(rerankModelProperties.getApiKey());
        return new DefaultRerankModel(config);
    }
}

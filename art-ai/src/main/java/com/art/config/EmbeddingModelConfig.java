package com.art.config;

import com.agentsflex.core.model.embedding.EmbeddingModel;
import com.agentsflex.embedding.ollama.OllamaEmbeddingConfig;
import com.agentsflex.embedding.ollama.OllamaEmbeddingModel;
import com.agentsflex.embedding.openai.OpenAIEmbeddingConfig;
import com.agentsflex.embedding.openai.OpenAIEmbeddingModel;
import com.art.properties.EmbeddingModelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * 嵌入模型实例
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Slf4j
@Component
@Configuration
@DependsOn("embeddingModelProperties")
public class EmbeddingModelConfig {
    /**
     * 嵌入模型配置
     */
    private final EmbeddingModelProperties embeddingModelProperties;

    /**
     * 构造函数
     *
     * @param embeddingModelProperties 嵌入模型配置
     */
    public EmbeddingModelConfig(EmbeddingModelProperties embeddingModelProperties) {
        this.embeddingModelProperties = embeddingModelProperties;
    }

    /**
     * 嵌入模型
     *
     * @return {@link EmbeddingModel} 嵌入模型对象
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        String provider = embeddingModelProperties.getProvider();
        EmbeddingModel model;
        switch (provider) {
            case "ollama":
                OllamaEmbeddingConfig ollamaConfig = new OllamaEmbeddingConfig();
                ollamaConfig.setModel(embeddingModelProperties.getModel());
                ollamaConfig.setEndpoint(embeddingModelProperties.getEndpoint());
                ollamaConfig.setRequestPath(embeddingModelProperties.getRequestPath());
                model = new OllamaEmbeddingModel(ollamaConfig);
                break;
            case "openAi":
                OpenAIEmbeddingConfig OpenAiConfig = new OpenAIEmbeddingConfig();
                OpenAiConfig.setModel(embeddingModelProperties.getModel());
                OpenAiConfig.setEndpoint(embeddingModelProperties.getEndpoint());
                OpenAiConfig.setRequestPath(embeddingModelProperties.getRequestPath());
                OpenAiConfig.setApiKey(embeddingModelProperties.getApiKey());
                model = new OpenAIEmbeddingModel(OpenAiConfig);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported embedding model provider: " + provider + ",it should be openAi | ollama.");
        }
        log.info("Embedding model initialized successfully.");
        return model;
    }
}

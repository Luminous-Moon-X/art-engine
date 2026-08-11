package com.art.config;

import com.agentsflex.core.model.embedding.EmbeddingModel;
import com.agentsflex.store.pgvector.PgvectorVectorStore;
import com.agentsflex.store.pgvector.PgvectorVectorStoreConfig;
import com.art.properties.VectorStoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * 向量库自动配置类
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Slf4j
@Component
@Configuration
@DependsOn({"vectorStoreProperties", "embeddingModelProperties"})
public class VectorStoreAutoConfig {
    /**
     * 向量库配置
     */
    private final VectorStoreProperties vectorStoreProperties;

    /**
     * 嵌入模型
     */
    private final EmbeddingModel embeddingModel;

    /**
     * 构造函数
     *
     * @param vectorStoreProperties 向量库配置
     * @param embeddingModel        嵌入模型
     */
    public VectorStoreAutoConfig(VectorStoreProperties vectorStoreProperties, EmbeddingModel embeddingModel) {
        this.vectorStoreProperties = vectorStoreProperties;
        this.embeddingModel = embeddingModel;
    }

    /**
     * 向量库存储对象实例
     *
     * @return {@link PgvectorVectorStore} 向量库存储对象实例
     */
    @Bean
    public PgvectorVectorStore vectorStore() {
        PgvectorVectorStoreConfig config = new PgvectorVectorStoreConfig();
        config.setHost(vectorStoreProperties.getHost());
        config.setPort(vectorStoreProperties.getPort());
        config.setDatabaseName(vectorStoreProperties.getDatabase());
        config.setUsername(vectorStoreProperties.getUsername());
        config.setPassword(vectorStoreProperties.getPassword());
        if (vectorStoreProperties.getCollectionName() != null) {
            config.setDefaultCollectionName(vectorStoreProperties.getCollectionName());
        }
        config.setVectorDimension(vectorStoreProperties.getDimension());
        config.setAutoCreateCollection(vectorStoreProperties.getAutoCreateCollection());
        config.setUseHnswIndex(vectorStoreProperties.getUseHnswIndex());
        PgvectorVectorStore store = new PgvectorVectorStore(config);
        store.setEmbeddingModel(embeddingModel);
        log.info("Vector store initialized successfully.");
        return store;
    }
}

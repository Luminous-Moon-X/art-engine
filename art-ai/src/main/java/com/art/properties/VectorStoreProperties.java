package com.art.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 向量库配置
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Getter
@Setter
@NoArgsConstructor
@Component("vectorStoreProperties")
@ConfigurationProperties(prefix = "art.vector-db")
public class VectorStoreProperties {
    /**
     * 库地址
     */
    private String host;
    /**
     * 端口
     */
    private Integer port;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 数据库
     */
    private String database = "agent_vector";
    /**
     * 默认表名
     */
    private String collectionName = null;
    /**
     * 向量维度
     */
    private Integer dimension;
    /**
     * 自动创建表
     */
    private Boolean autoCreateCollection;
    /**
     * 创建HNSW索引
     */
    private Boolean useHnswIndex;
}

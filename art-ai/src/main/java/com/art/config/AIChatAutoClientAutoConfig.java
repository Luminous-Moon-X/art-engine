package com.art.config;

import com.agentsflex.core.model.chat.ChatModel;
import com.agentsflex.model.chat.openai.OpenAIChatConfig;
import com.agentsflex.model.chat.openai.OpenAIChatModel;
import com.art.properties.BigModelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * 文字AI对话客户端自动配置类
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Slf4j
@Component
@Configuration
@DependsOn("artModelProperties")
public class AIChatAutoClientAutoConfig {

    /**
     * 自定义模型配置
     */
    private final BigModelProperties artModelProperties;

    /**
     * 构造函数
     *
     * @param artModelProperties 自定义模型配置
     */
    public AIChatAutoClientAutoConfig(BigModelProperties artModelProperties) {
        this.artModelProperties = artModelProperties;
    }


    /**
     * 文字AI对话客户端
     *
     * @return 文字AI对话客户端
     */
    @Bean
    public ChatModel artChatClient() {
        OpenAIChatConfig config = new OpenAIChatConfig();
        config.setApiKey(artModelProperties.getApiKey());
        config.setModel(artModelProperties.getModel());
        config.setEndpoint(artModelProperties.getEndpoint());
        config.setRequestPath(artModelProperties.getRequestPath());
        config.setProvider(artModelProperties.getProvider());
        config.setSupportImage(artModelProperties.getSupportImage()); // 禁用图片
        config.setSupportAudio(artModelProperties.getSupportAudio());
        config.setSupportVideo(artModelProperties.getSupportVideo());
        config.setSupportImageBase64Only(artModelProperties.getOnlyBase64Image());
        config.setSupportThinking(artModelProperties.getSupportThinking());
        config.setThinkingEnabled(artModelProperties.getThinkingEnabled());
        config.setLogEnabled(false);  // 关闭日志
        OpenAIChatModel openAIChatModel = new OpenAIChatModel(config);
        log.info("Init big model configuration,provider:{}，model:{}", artModelProperties.getProvider(), artModelProperties.getModel());
        return openAIChatModel;
    }

}

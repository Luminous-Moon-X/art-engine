package com.art.config;

import com.agentsflex.core.model.chat.ChatModel;
import com.agentsflex.model.chat.openai.OpenAIChatConfig;
import com.agentsflex.model.chat.openai.OpenAIChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * 文字AI对话客户端自动配置类
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Component
@Configuration
public class AIChatAutoClientAutoConfig {


    /**
     * 文字AI对话客户端
     *
     * @return 文字AI对话客户端
     */
    @Bean
    public ChatModel artChatClient() {
        OpenAIChatConfig config = new OpenAIChatConfig();
        config.setApiKey("sk-xQap5kp3ubSFu9FdfTgpAPm5yVQDEeIM3oYTNKN0yqB6vYfctaGcmTS4atTJ4FVU");
        config.setModel("deepseek-v4-pro");
        config.setEndpoint("https://opencode.ai/zen/go");
        config.setRequestPath("/v1/chat/completions");
        config.setProvider("openai");
        config.setSupportImage(false); // 禁用图片
        config.setLogEnabled(false);  // 关闭日志

        return new OpenAIChatModel(config);
    }

}

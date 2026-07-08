package com.art;

import com.agentsflex.core.model.chat.ChatConfig;
import com.agentsflex.core.model.chat.OpenAICompatibleChatModel;

public class ChatConfigTest {
    public static void main(String[] args) {
        ChatConfig config = new ChatConfig();
        config.setApiKey("sk-xQap5kp3ubSFu9FdfTgpAPm5yVQDEeIM3oYTNKN0yqB6vYfctaGcmTS4atTJ4FVU");
        config.setModel("deepseek-v4-pro");
        config.setEndpoint("https://opencode.ai");
        config.setRequestPath("/zen/go/v1/chat/completions");
        config.setProvider("openai");
        // OpenAICompatibleChatModel 是 BaseChatModel 的具体实现类，
        // 内部会自动创建 OpenAIChatClient 和 OpenAIChatRequestSpecBuilder
        OpenAICompatibleChatModel<ChatConfig> chatModel = new OpenAICompatibleChatModel<>(config);
        String result = chatModel.chat("你好，你是谁，请随便给我说个笑话！");
        System.out.println(result);
    }
}

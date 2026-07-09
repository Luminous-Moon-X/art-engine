package com.art.service.impl;

import com.agentsflex.core.message.SystemMessage;
import com.agentsflex.core.model.chat.ChatModel;
import com.agentsflex.core.model.chat.StreamResponseListener;
import com.agentsflex.core.model.chat.response.AiMessageResponse;
import com.agentsflex.core.model.client.StreamContext;
import com.agentsflex.core.prompt.SimplePrompt;
import com.art.prompt.SystemPromptProvider;
import com.art.service.AIChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * AI对话服务实现类
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Slf4j
@Service
public class AIChatServiceImpl implements AIChatService {

    /**
     * AI模型对象
     */
    private final ChatModel artChatClient;

    /**
     * 系统提示词提供者
     */
    private final SystemPromptProvider systemPromptProvider;

    /**
     * 构造器
     *
     * @param artChatClient         AI模型对象
     * @param systemPromptProvider 系统提示词提供者
     */
    public AIChatServiceImpl(ChatModel artChatClient, SystemPromptProvider systemPromptProvider) {
        this.artChatClient = artChatClient;
        this.systemPromptProvider = systemPromptProvider;
    }

    /**
     * AI对话
     *
     * @param quest 输入内容
     * @return SSE链接
     */
    @Override
    public SseEmitter chat(String quest) {
        // 超时时间5分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        // 构建提示词：塞入系统提示词 + 用户输入
        SimplePrompt prompt = new SimplePrompt(quest);
        prompt.setSystemMessage(new SystemMessage(systemPromptProvider.getSystemPrompt()));

        artChatClient.chatStream(prompt, new StreamResponseListener() {
            @Override
            public void onMessage(StreamContext context, AiMessageResponse response) {
                try {
                    if (response == null || response.getMessage() == null) {
                        return;
                    }
                    String content = response.getMessage().getContent();
                    if (content != null) {
                        // 推送增量内容
                        emitter.send(SseEmitter.event().data(content));
                    }
                } catch (IOException e) {
                    log.error("SSE 推送消息失败", e);
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onStop(StreamContext context) {
                emitter.complete();
            }
        });

        // 客户端超时或断开连接时回收资源
        emitter.onTimeout(emitter::complete);
        emitter.onError(throwable -> log.warn("SSE 连接异常: {}", throwable.toString()));

        return emitter;
    }
}

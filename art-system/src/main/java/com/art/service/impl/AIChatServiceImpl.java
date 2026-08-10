package com.art.service.impl;

import com.agentsflex.core.message.Message;
import com.agentsflex.core.model.chat.ChatModel;
import com.agentsflex.core.model.chat.StreamResponseListener;
import com.agentsflex.core.model.chat.response.AiMessageResponse;
import com.agentsflex.core.model.client.StreamContext;
import com.agentsflex.core.prompt.MemoryPrompt;
import com.art.domain.vo.UserChatVO;
import com.art.memory.RedisChatMemory;
import com.art.prompt.SystemPromptProvider;
import com.art.service.AIChatService;
import com.art.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
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
     * Redis客户端 对话记忆
     */
    private final RedisTemplate<String, Message> redisTemplate;

    /**
     * 对话记忆key
     */
    private static final String AI_CHAT_MEMORY_KEY = "ai_chat_memory";

    /**
     * 构造器
     *
     * @param artChatClient        AI模型对象
     * @param systemPromptProvider 系统提示词提供者
     * @param messageRedisTemplate Redis客户端
     */
    public AIChatServiceImpl(ChatModel artChatClient, SystemPromptProvider systemPromptProvider, RedisTemplate<String, Message> messageRedisTemplate) {
        this.artChatClient = artChatClient;
        this.systemPromptProvider = systemPromptProvider;
        this.redisTemplate = messageRedisTemplate;
    }

    /**
     * AI对话
     *
     * @param userChatVO 用户对话对象
     * @return SSE链接
     */
    @Override
    public SseEmitter chat(UserChatVO userChatVO) {
        String chatId = userChatVO.getChatId();
        String question = userChatVO.getQuestion();
        // 超时时间5分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        // 创建对话记忆
        String conversationId = AI_CHAT_MEMORY_KEY + ":" + SecurityUtil.getUserId() + ":" + chatId;
        RedisChatMemory memory = new RedisChatMemory(conversationId, redisTemplate);
        // 构建提示词：塞入系统提示词 + 用户输入
        MemoryPrompt prompt = new MemoryPrompt(memory);
        prompt.setSystemMessage(systemPromptProvider.getSystemPrompt());
        prompt.addUserMessage(question);
        // 发送流式请求
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

            // 流式结束
            @Override
            public void onClose(StreamContext context) {
                prompt.addMessage(context.getFullMessage());
                // 将AI回答添加到对话记忆
                emitter.complete();
            }
        });

        // 客户端超时或断开连接时回收资源
        emitter.onTimeout(emitter::complete);
        emitter.onError(throwable -> {
            log.warn("SSE 连接异常: {}", throwable.toString());
            emitter.complete();
        });
        return emitter;
    }
}

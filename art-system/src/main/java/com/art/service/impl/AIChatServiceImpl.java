package com.art.service.impl;

import com.agentsflex.core.document.Document;
import com.agentsflex.core.message.AiMessage;
import com.agentsflex.core.message.Message;
import com.agentsflex.core.model.chat.ChatModel;
import com.agentsflex.core.model.chat.StreamResponseListener;
import com.agentsflex.core.model.chat.response.AiMessageResponse;
import com.agentsflex.core.model.client.StreamContext;
import com.agentsflex.core.prompt.MemoryPrompt;
import com.agentsflex.core.store.SearchWrapper;
import com.agentsflex.store.pgvector.PgvectorVectorStore;
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
import java.util.List;

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

    private final PgvectorVectorStore vectorStore;

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
     * @param vectorStore          向量数据库
     */
    public AIChatServiceImpl(ChatModel artChatClient, SystemPromptProvider systemPromptProvider, RedisTemplate<String, Message> messageRedisTemplate, PgvectorVectorStore vectorStore) {
        this.artChatClient = artChatClient;
        this.systemPromptProvider = systemPromptProvider;
        this.redisTemplate = messageRedisTemplate;
        this.vectorStore = vectorStore;
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
        // RAG知识库查询
        SearchWrapper wrapper = new SearchWrapper()
                .text(question).maxResults(10).minScore(0.8);
        List<Document> ragDocuments = vectorStore.search(wrapper);
        // TODO 使用重排模型 优化匹配精准度
        StringBuilder ragContent = new StringBuilder();
        // TODO 使用结构化的数据拼接 XML格式
        ragDocuments.forEach(document -> ragContent.append(document.getContent()));
        AiMessage aiMessage = new AiMessage(ragContent.toString());
        prompt.addMessageTemporary(aiMessage);
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
                prompt.clearTemporaryMessages();
                // 将AI回答添加到对话记忆
                emitter.complete();
            }
        });

        // 客户端超时或断开连接时回收资源
        emitter.onTimeout(emitter::complete);
        emitter.onError(throwable -> {
            log.warn("SSE 连接异常: {}", throwable.toString());
            emitter.complete();
            prompt.clearTemporaryMessages();
        });
        return emitter;
    }
}

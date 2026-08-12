package com.art.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.agentsflex.core.document.Document;
import com.agentsflex.core.message.AiMessage;
import com.agentsflex.core.message.Message;
import com.agentsflex.core.model.chat.ChatModel;
import com.agentsflex.core.model.chat.StreamResponseListener;
import com.agentsflex.core.model.chat.response.AiMessageResponse;
import com.agentsflex.core.model.client.StreamContext;
import com.agentsflex.core.prompt.MemoryPrompt;
import com.agentsflex.core.store.SearchWrapper;
import com.agentsflex.core.store.StoreResult;
import com.agentsflex.rerank.DefaultRerankModel;
import com.agentsflex.store.pgvector.PgvectorVectorStore;
import com.art.DocumentUtil;
import com.art.domain.vo.UserChatVO;
import com.art.exception.ArtException;
import com.art.memory.RedisChatMemory;
import com.art.prompt.SystemPromptProvider;
import com.art.service.AIChatService;
import com.art.utils.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
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
    /**
     * 向量数据库
     */
    private final PgvectorVectorStore vectorStore;

    /**
     * 重排模型
     */
    private final DefaultRerankModel rerankModel;

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
     * @param rerankModel          重排模型
     */
    public AIChatServiceImpl(ChatModel artChatClient, SystemPromptProvider systemPromptProvider,
                             RedisTemplate<String, Message> messageRedisTemplate, PgvectorVectorStore vectorStore, @Nullable DefaultRerankModel rerankModel) {
        this.artChatClient = artChatClient;
        this.systemPromptProvider = systemPromptProvider;
        this.redisTemplate = messageRedisTemplate;
        this.vectorStore = vectorStore;
        this.rerankModel = rerankModel;
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
        // 构建prompt提示词
        MemoryPrompt prompt = buildPrompt(conversationId, question);
        // 发送流式请求
        artChatClient.chatStream(prompt, new StreamResponseListener() {
            @Override
            public void onMessage(StreamContext context, AiMessageResponse response) {
                try {
                    if (response == null || response.getMessage() == null) {
                        return;
                    }
                    String content = response.getMessage().getContent();
                    if (content != null && !"[DONE]".equals(response.getRawText())) {
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


    /**
     * 构建prompt
     *
     * @param conversationId 对话id
     * @param question       输入问题
     * @return prompt
     */
    private MemoryPrompt buildPrompt(String conversationId, String question) {
        RedisChatMemory memory = new RedisChatMemory(conversationId, redisTemplate);
        // 构建提示词：塞入系统提示词 + 用户输入
        MemoryPrompt prompt = new MemoryPrompt(memory);
        prompt.setSystemMessage(systemPromptProvider.getSystemPrompt());
        prompt.addUserMessage(question);
        // RAG知识库查询
        SearchWrapper wrapper = new SearchWrapper()
                .text(question).maxResults(20).minScore(0.2).outputVector(true);
        List<Document> ragDocuments = vectorStore.search(wrapper);
        // 重排模型优化精确度
        if (CollectionUtil.isNotEmpty(ragDocuments)) {
            if (rerankModel != null) {
                ragDocuments = rerankModel.rerank(question, ragDocuments);
            }
            ragDocuments.subList(0, Math.min(ragDocuments.size(), 5));
            // 使用XML格式的结构化数据
            StringBuilder ragContent = new StringBuilder("<context>");
            ragDocuments.forEach(document -> {
                ragContent.append(String.format("<doc id=\"%s\" title=\"%s\">", document.getId(), document.getTitle()));
                ragContent.append(document.getContent());
                ragContent.append("</doc>");
            });
            ragContent.append("</context>");
            // RAG检索内容作为过程消息 不入记忆
            AiMessage aiMessage = new AiMessage(ragContent.toString());
            prompt.addMessageTemporary(aiMessage);
        }
        return prompt;
    }

    /**
     * 向量化处理文档
     *
     * @param file 文档
     */
    @Override
    public void vectorDoc(MultipartFile file) {
        String documentText;
        try {
            // 解析文档内容
            documentText = DocumentUtil.extract(file);
        } catch (IOException e) {
            throw new ArtException("文档解析失败：", e);
        }
        String fileName = file.getOriginalFilename();
        List<Document> documents = new ArrayList<>();
        // 大模型语义分割对文档最大长度有限制，先根据最大长度分割后再循坏处理向量化存储
        if (documentText.length() > 10000) {
            List<String> docSplit = splitFixedLength(documentText, 10000);
            docSplit.forEach(doc -> {
                Document document = Document.of(doc);
                document.setTitle(fileName);
                documents.addAll(DocumentUtil.splitAi(document, artChatClient));
            });
        } else {
            Document document = Document.of(documentText);
            document.setTitle(fileName);
            // 按照段落拆分文档
            documents.addAll(DocumentUtil.splitAi(document, artChatClient));
        }
        StoreResult store = vectorStore.store(documents);
        if (store.getException() != null) {
            throw new ArtException("文档向量化失败：", store.getException());
        }
        log.info("文档向量化成功：{}", fileName);
    }


    /**
     * 文档长度大于语义分割要求最大长度时，根据最大长度分割文档
     *
     * @param documentText 文档内容
     * @param chunkSize    最大长度
     * @return 分割后的文档列表
     */
    public static List<String> splitFixedLength(String documentText, int chunkSize) {
        int len = documentText.length();
        List<String> chunks = new ArrayList<>((len + chunkSize - 1) / chunkSize);
        for (int i = 0; i < len; i += chunkSize) {
            int end = Math.min(len, i + chunkSize);
            chunks.add(documentText.substring(i, end));
        }
        return chunks;
    }

}

package com.art.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.agentsflex.core.document.Document;
import com.agentsflex.core.message.AiMessage;
import com.agentsflex.core.message.Message;
import com.agentsflex.core.message.UserMessage;
import com.agentsflex.core.model.chat.ChatModel;
import com.agentsflex.core.model.chat.StreamResponseListener;
import com.agentsflex.core.model.chat.response.AiMessageResponse;
import com.agentsflex.core.model.client.StreamContext;
import com.agentsflex.core.prompt.MemoryPrompt;
import com.agentsflex.core.store.SearchWrapper;
import com.agentsflex.core.store.StoreResult;
import com.agentsflex.rerank.DefaultRerankModel;
import com.agentsflex.store.pgvector.PgvectorVectorStore;
import com.alibaba.fastjson2.JSON;
import com.art.DocumentUtil;
import com.art.domain.AiChatMessage;
import com.art.domain.AiConversation;
import com.art.domain.vo.ChatMessageVO;
import com.art.domain.vo.ConversationVO;
import com.art.domain.vo.UserChatVO;
import com.art.exception.ArtException;
import com.art.mapper.AiChatMessageMapper;
import com.art.mapper.AiConversationMapper;
import com.art.memory.RedisChatMemory;
import com.art.prompt.SystemPromptProvider;
import com.art.service.AIChatService;
import com.art.utils.ConvertUtil;
import com.art.utils.SecurityUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
     * 对话表映射层
     */
    private final AiConversationMapper aiConversationMapper;

    /**
     * 对话内容表映射层
     */
    private final AiChatMessageMapper aiChatMessageMapper;

    /**
     * 对话记忆key前缀
     */
    private static final String AI_CHAT_MEMORY_KEY = "ai_chat_memory";

    /**
     * 默认对话名称
     */
    private static final String DEFAULT_CONVERSATION_NAME = "新对话";

    /**
     * 对话主题最大长度
     */
    private static final int MAX_TITLE_LENGTH = 10;

    /**
     * 主题总结时取AI回答的最大长度
     */
    private static final int MAX_TITLE_CONTEXT_LENGTH = 500;

    /**
     * 构造器
     *
     * @param artChatClient         AI模型对象
     * @param systemPromptProvider  系统提示词提供者
     * @param messageRedisTemplate  Redis客户端
     * @param vectorStore           向量数据库
     * @param rerankModel           重排模型
     * @param aiConversationMapper  对话表映射层
     * @param aiChatMessageMapper   对话内容表映射层
     */
    public AIChatServiceImpl(ChatModel artChatClient, SystemPromptProvider systemPromptProvider,
                             RedisTemplate<String, Message> messageRedisTemplate, PgvectorVectorStore vectorStore,
                             @Nullable DefaultRerankModel rerankModel, AiConversationMapper aiConversationMapper,
                             AiChatMessageMapper aiChatMessageMapper) {
        this.artChatClient = artChatClient;
        this.systemPromptProvider = systemPromptProvider;
        this.redisTemplate = messageRedisTemplate;
        this.vectorStore = vectorStore;
        this.rerankModel = rerankModel;
        this.aiConversationMapper = aiConversationMapper;
        this.aiChatMessageMapper = aiChatMessageMapper;
    }

    /**
     * AI对话
     *
     * <p>SSE事件约定：
     * <ul>
     *     <li>event: message —— data为AI回答的增量内容（JSON字符串）</li>
     *     <li>event: done —— data为对话信息（对话ID、对话名称、轮次数）</li>
     *     <li>event: error —— data为错误信息</li>
     * </ul></p>
     *
     * @param userChatVO 用户对话对象
     * @return SSE链接
     */
    @Override
    public SseEmitter chat(UserChatVO userChatVO) {
        String chatId = userChatVO.getChatId();
        String question = userChatVO.getQuestion();
        if (StrUtil.isBlank(chatId)) {
            throw new ArtException("对话ID不能为空");
        }
        if (StrUtil.isBlank(question)) {
            throw new ArtException("问题内容不能为空");
        }
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new ArtException(401, "用户未登录或登录已过期");
        }
        // 超时时间5分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        // 保存用户消息
        saveMessage(chatId, AiChatMessage.TYPE_USER, question, userId);
        // 创建对话记忆
        String memoryKey = AI_CHAT_MEMORY_KEY + ":" + userId + ":" + chatId;
        // 构建prompt提示词
        MemoryPrompt prompt = buildPrompt(memoryKey, chatId, question);
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
                        // 推送增量内容（JSON编码保证SSE单行data）
                        emitter.send(SseEmitter.event().name("message").data(JSON.toJSONString(content)));
                    }
                } catch (Exception e) {
                    log.error("SSE 推送消息失败", e);
                    emitter.completeWithError(e);
                }
            }

            // 流式结束
            @Override
            public void onClose(StreamContext context) {
                try {
                    handleStreamClose(emitter, prompt, context, chatId, question, userId);
                } catch (Exception e) {
                    log.error("AI对话结束处理失败", e);
                    sendErrorAndComplete(emitter, "AI服务异常，请稍后重试");
                }
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
     * 处理流式对话结束后的业务逻辑：落库消息、创建/更新对话、总结主题、推送完成事件
     *
     * @param emitter   SSE连接
     * @param prompt    对话提示词
     * @param context   流式上下文
     * @param chatId    对话ID
     * @param question  用户问题
     * @param userId    用户ID
     */
    private void handleStreamClose(SseEmitter emitter, MemoryPrompt prompt, StreamContext context,
                                   String chatId, String question, Long userId) {
        AiMessage aiMessage = context.getFullMessage();
        if (aiMessage != null) {
            prompt.addMessage(aiMessage);
        }
        if (context.isError()) {
            log.error("AI流式对话发生异常", context.getThrowable());
            sendErrorAndComplete(emitter, "AI服务异常，请稍后重试");
            return;
        }
        String answer = aiMessage == null ? null : aiMessage.getContent();
        // 校验对话归属，防止越权写入他人对话
        AiConversation conversation = aiConversationMapper.selectOneByQuery(
                QueryWrapper.create().eq(AiConversation::getConversationId, chatId));
        if (conversation != null && !Objects.equals(userId, conversation.getUserId())) {
            sendErrorAndComplete(emitter, "无权访问该对话");
            return;
        }
        // 保存AI回答
        if (StrUtil.isNotBlank(answer)) {
            saveMessage(chatId, AiChatMessage.TYPE_AI, answer, userId);
        }
        // 处理对话表：新对话在收到AI回答后落库，旧对话轮次+1
        int turnCount;
        if (conversation == null) {
            conversation = new AiConversation();
            conversation.setUserId(userId);
            conversation.setConversationId(chatId);
            conversation.setConversationName(DEFAULT_CONVERSATION_NAME);
            conversation.setTurnCount(1);
            conversation.setCreateId(userId);
            conversation.setCreateTime(LocalDateTime.now());
            conversation.setDeleteFlag(0);
            aiConversationMapper.insert(conversation);
            turnCount = 1;
        } else {
            turnCount = (conversation.getTurnCount() == null ? 0 : conversation.getTurnCount()) + 1;
            AiConversation turnUpdate = new AiConversation();
            turnUpdate.setId(conversation.getId());
            turnUpdate.setTurnCount(turnCount);
            turnUpdate.setUpdateId(userId);
            aiConversationMapper.update(turnUpdate);
            conversation.setTurnCount(turnCount);
        }
        // 首轮问答完成后，根据第一轮问题和回答调用AI总结对话主题（不超过10个字）
        if (turnCount == 1) {
            String title = generateConversationTitle(question, answer);
            AiConversation titleUpdate = new AiConversation();
            titleUpdate.setId(conversation.getId());
            titleUpdate.setConversationName(title);
            titleUpdate.setUpdateId(userId);
            aiConversationMapper.update(titleUpdate);
            conversation.setConversationName(title);
        }
        // 推送完成事件，携带最新对话信息
        Map<String, Object> doneEvent = new HashMap<>();
        doneEvent.put("conversationId", chatId);
        doneEvent.put("conversationName", conversation.getConversationName());
        doneEvent.put("turnCount", turnCount);
        try {
            emitter.send(SseEmitter.event().name("done").data(JSON.toJSONString(doneEvent)));
            emitter.complete();
        } catch (Exception e) {
            log.error("SSE 推送完成事件失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 查询当前登录用户的所有对话
     *
     * @return 对话列表（按创建时间倒序）
     */
    @Override
    public List<ConversationVO> listConversations() {
        Long userId = getRequiredUserId();
        List<AiConversation> list = aiConversationMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq(AiConversation::getUserId, userId)
                        .orderBy(AiConversation::getCreateTime, false)
                        .orderBy(AiConversation::getId, false));
        List<ConversationVO> result = ConvertUtil.convertList(list, ConversationVO.class);
        result.forEach(item -> {
            if (StrUtil.isBlank(item.getConversationName())) {
                item.setConversationName(DEFAULT_CONVERSATION_NAME);
            }
        });
        return result;
    }

    /**
     * 查询指定对话的全部消息内容
     *
     * @param conversationId 对话ID
     * @return 消息列表（按时间正序）
     */
    @Override
    public List<ChatMessageVO> listMessages(String conversationId) {
        Long userId = getRequiredUserId();
        if (StrUtil.isBlank(conversationId)) {
            return Collections.emptyList();
        }
        // 校验对话归属，防止越权读取他人对话
        AiConversation conversation = aiConversationMapper.selectOneByQuery(
                QueryWrapper.create().eq(AiConversation::getConversationId, conversationId));
        if (conversation != null && !Objects.equals(userId, conversation.getUserId())) {
            throw new ArtException(403, "无权访问该对话");
        }
        List<AiChatMessage> messages = aiChatMessageMapper.selectListByQuery(
                QueryWrapper.create()
                        .eq(AiChatMessage::getConversationId, conversationId)
                        .orderBy(AiChatMessage::getId, true));
        return ConvertUtil.convertList(messages, ChatMessageVO.class);
    }

    /**
     * 构建prompt
     *
     * @param memoryKey  对话记忆key
     * @param chatId     对话id
     * @param question   输入问题
     * @return prompt
     */
    private MemoryPrompt buildPrompt(String memoryKey, String chatId, String question) {
        RedisChatMemory memory = new RedisChatMemory(memoryKey, redisTemplate);
        // Redis记忆为空时（如服务重启、缓存过期），从数据库回填历史消息，保证多轮对话上下文不丢失
        if (CollectionUtil.isEmpty(memory.getMessages(Integer.MAX_VALUE))) {
            List<AiChatMessage> history = aiChatMessageMapper.selectListByQuery(
                    QueryWrapper.create()
                            .eq(AiChatMessage::getConversationId, chatId)
                            .orderBy(AiChatMessage::getId, true));
            if (CollectionUtil.isNotEmpty(history)) {
                history.forEach(item -> {
                    if (AiChatMessage.TYPE_USER.equals(item.getMessageType())) {
                        memory.addMessage(new UserMessage(item.getContent()));
                    } else {
                        memory.addMessage(new AiMessage(item.getContent()));
                    }
                });
            }
        }
        // 构建提示词：塞入系统提示词 + 用户输入
        MemoryPrompt prompt = new MemoryPrompt(memory);
        prompt.setSystemMessage(systemPromptProvider.getSystemPrompt());
        prompt.addUserMessage(question);
        // RAG知识库查询（知识库服务不可用时降级为普通对话，不影响主流程）
        try {
            attachRagContext(prompt, question);
        } catch (Exception e) {
            log.warn("RAG知识库查询失败，已降级为普通对话: {}", e.getMessage());
        }
        return prompt;
    }

    /**
     * 查询RAG知识库并将检索内容作为过程消息（不入记忆）附加到提示词
     *
     * @param prompt   对话提示词
     * @param question 用户问题
     */
    private void attachRagContext(MemoryPrompt prompt, String question) {
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
    }

    /**
     * 保存对话消息
     *
     * @param chatId      对话ID
     * @param messageType 消息类型 USER/AI
     * @param content     消息内容
     * @param userId      用户ID
     */
    private void saveMessage(String chatId, String messageType, String content, Long userId) {
        AiChatMessage message = new AiChatMessage();
        message.setConversationId(chatId);
        message.setMessageType(messageType);
        message.setContent(content);
        message.setCreateId(userId);
        message.setCreateTime(LocalDateTime.now());
        message.setDeleteFlag(0);
        aiChatMessageMapper.insert(message);
    }

    /**
     * 根据第一轮问题和回答调用AI总结对话主题
     *
     * @param question 用户问题
     * @param answer   AI回答
     * @return 对话主题（不超过10个字）
     */
    private String generateConversationTitle(String question, String answer) {
        String fallback = buildFallbackTitle(question);
        try {
            String titlePrompt = "请根据以下对话中用户的问题和AI的回答，总结出一个不超过10个字的对话主题。"
                    + "直接输出主题本身，不要输出引号、书名号、标点符号或任何解释性文字。\n"
                    + "用户问题：" + question + "\n"
                    + "AI回答：" + abbreviate(answer);
            String title = artChatClient.chat(titlePrompt);
            if (StrUtil.isBlank(title)) {
                return fallback;
            }
            // 清理引号、标点、符号与空白
            title = title.replaceAll("[\\p{P}\\p{S}\\s]", "");
            if (StrUtil.isBlank(title)) {
                return fallback;
            }
            // AI回复可能存在误差，这里多考虑5个字
            return title.length() > MAX_TITLE_LENGTH + 5 ? title.substring(0, MAX_TITLE_LENGTH + 5) : title;
        } catch (Exception e) {
            log.error("调用AI总结对话主题失败", e);
            return fallback;
        }
    }

    /**
     * 构建兜底对话主题（取问题前10个字）
     *
     * @param question 用户问题
     * @return 兜底主题
     */
    private String buildFallbackTitle(String question) {
        String cleaned = question == null ? "" : question.replaceAll("[\\p{P}\\p{S}\\s]", "");
        if (cleaned.isEmpty()) {
            return DEFAULT_CONVERSATION_NAME;
        }
        return cleaned.length() > MAX_TITLE_LENGTH ? cleaned.substring(0, MAX_TITLE_LENGTH) : cleaned;
    }

    /**
     * 截断字符串
     *
     * @param text 原始字符串
     * @return 截断后的字符串
     */
    private String abbreviate(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        return text.length() > AIChatServiceImpl.MAX_TITLE_CONTEXT_LENGTH ? text.substring(0, AIChatServiceImpl.MAX_TITLE_CONTEXT_LENGTH) : text;
    }

    /**
     * 发送错误事件并关闭连接
     *
     * @param emitter SSE连接
     * @param message 错误信息
     */
    private void sendErrorAndComplete(SseEmitter emitter, String message) {
        try {
            Map<String, Object> errorEvent = new HashMap<>();
            errorEvent.put("message", message);
            emitter.send(SseEmitter.event().name("error").data(JSON.toJSONString(errorEvent)));
            emitter.complete();
        } catch (Exception e) {
            log.error("SSE 推送错误事件失败", e);
            emitter.completeWithError(e);
        }
    }

    /**
     * 获取当前登录用户ID，未登录时抛出异常
     *
     * @return 用户ID
     */
    private Long getRequiredUserId() {
        Long userId = SecurityUtil.getUserId();
        if (userId == null) {
            throw new ArtException(401, "用户未登录或登录已过期");
        }
        return userId;
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

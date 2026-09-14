package com.art.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.transaction.annotation.Transactional;
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
import com.agentsflex.rerank.DefaultRerankModel;
import com.agentsflex.store.pgvector.PgvectorVectorStore;
import com.alibaba.fastjson2.JSON;
import com.art.context.SecurityContextHolder;
import com.art.domain.AiChatMessage;
import com.art.domain.AiConversation;
import com.art.domain.KnowledgeBase;
import com.art.domain.vo.ChatMessageVO;
import com.art.domain.vo.ConversationVO;
import com.art.domain.vo.UserChatVO;
import com.art.exception.ArtException;
import com.art.mapper.AiChatMessageMapper;
import com.art.mapper.AiConversationMapper;
import com.art.mapper.KnowledgeBaseMapper;
import com.art.memory.RedisChatMemory;
import com.art.prompt.SystemPromptProvider;
import com.art.service.AIChatService;
import com.art.utils.ConvertUtil;
import com.art.utils.SecurityUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
     * 知识库Mapper（校验知识库归属、限定RAG检索范围）
     */
    private final KnowledgeBaseMapper knowledgeBaseMapper;

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
     * @param artChatClient        AI模型对象
     * @param systemPromptProvider 系统提示词提供者
     * @param messageRedisTemplate Redis客户端
     * @param vectorStore          向量数据库
     * @param rerankModel          重排模型
     * @param aiConversationMapper 对话表映射层
     * @param aiChatMessageMapper  对话内容表映射层
     * @param knowledgeBaseMapper  知识库Mapper
     */
    public AIChatServiceImpl(ChatModel artChatClient, SystemPromptProvider systemPromptProvider,
                             RedisTemplate<String, Message> messageRedisTemplate, PgvectorVectorStore vectorStore,
                             @Nullable DefaultRerankModel rerankModel, AiConversationMapper aiConversationMapper,
                             AiChatMessageMapper aiChatMessageMapper, KnowledgeBaseMapper knowledgeBaseMapper) {
        this.artChatClient = artChatClient;
        this.systemPromptProvider = systemPromptProvider;
        this.redisTemplate = messageRedisTemplate;
        this.vectorStore = vectorStore;
        this.rerankModel = rerankModel;
        this.aiConversationMapper = aiConversationMapper;
        this.aiChatMessageMapper = aiChatMessageMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
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
        Long kbId = userChatVO.getKbId();
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
        // 流式回调在OkHttp网络线程执行，需在请求线程捕获租户上下文并显式传入
        Long tenantId = SecurityUtil.getTenantId();
        // 超时时间5分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        // 保存用户消息
        saveMessage(chatId, AiChatMessage.TYPE_USER, question, userId);
        // 创建对话记忆
        String memoryKey = AI_CHAT_MEMORY_KEY + ":" + userId + ":" + chatId;
        // 构建prompt提示词
        MemoryPrompt prompt = buildPrompt(memoryKey, chatId, question, kbId);
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
                    handleStreamClose(emitter, prompt, context, chatId, question, userId, tenantId);
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
     * <p>回调可能由网络线程执行，此处显式补全租户/用户上下文，
     * 保证会话与消息落库时的 tenant_id 正确。</p>
     *
     * @param emitter  SSE连接
     * @param prompt   对话提示词
     * @param context  流式上下文
     * @param chatId   对话ID
     * @param question 用户问题
     * @param userId   用户ID
     * @param tenantId 生效租户ID
     */
    private void handleStreamClose(SseEmitter emitter, MemoryPrompt prompt, StreamContext context,
                                   String chatId, String question, Long userId, Long tenantId) {
        try {
            SecurityContextHolder.setTenantId(tenantId);
            SecurityContextHolder.setUserId(userId);
            this.handleStreamCloseInternal(emitter, prompt, context, chatId, question, userId);
        } finally {
            // 网络线程为池化线程，必须清理上下文，避免影响后续回调
            SecurityContextHolder.clear();
        }
    }

    /**
     * 处理流式对话结束后的业务逻辑（执行体）
     *
     * @param emitter  SSE连接
     * @param prompt   对话提示词
     * @param context  流式上下文
     * @param chatId   对话ID
     * @param question 用户问题
     * @param userId   用户ID
     */
    private void handleStreamCloseInternal(SseEmitter emitter, MemoryPrompt prompt, StreamContext context,
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
     * 重命名对话（校验归属）
     *
     * @param conversationId 对话ID
     * @param newName        新对话名称
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void renameConversation(String conversationId, String newName) {
        Long userId = getRequiredUserId();
        if (StrUtil.isBlank(conversationId)) {
            throw new ArtException("对话ID不能为空");
        }
        // 去除首尾空格，空名称不允许保存
        String name = newName == null ? "" : newName.trim();
        if (StrUtil.isBlank(name)) {
            throw new ArtException("对话名称不能为空");
        }
        AiConversation conversation = getOwnedConversation(conversationId, userId);
        AiConversation update = new AiConversation();
        update.setId(conversation.getId());
        update.setConversationName(name);
        update.setUpdateId(userId);
        aiConversationMapper.update(update);
    }

    /**
     * 删除对话及其全部消息（校验归属）
     *
     * @param conversationId 对话ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConversation(String conversationId) {
        Long userId = getRequiredUserId();
        if (StrUtil.isBlank(conversationId)) {
            throw new ArtException("对话ID不能为空");
        }
        // 校验归属并确认对话存在
        AiConversation conversation = getOwnedConversation(conversationId, userId);
        // 删除对话下的全部消息
        aiChatMessageMapper.deleteByQuery(QueryWrapper.create()
                .eq(AiChatMessage::getConversationId, conversationId));
        // 删除对话
        aiConversationMapper.deleteById(conversation.getId());
        // 清除Redis中的该对话记忆
        String memoryKey = AI_CHAT_MEMORY_KEY + ":" + userId + ":" + conversationId;
        try {
            redisTemplate.delete(memoryKey);
        } catch (Exception e) {
            log.warn("清除对话记忆失败: {}", e.getMessage());
        }
    }

    /**
     * 查询当前用户拥有的对话，不存在或不属于当前用户时抛异常
     *
     * @param conversationId 对话ID
     * @param userId         当前用户ID
     * @return 对话实体
     */
    private AiConversation getOwnedConversation(String conversationId, Long userId) {
        AiConversation conversation = aiConversationMapper.selectOneByQuery(
                QueryWrapper.create().eq(AiConversation::getConversationId, conversationId));
        if (conversation == null) {
            throw new ArtException("对话不存在或已被删除");
        }
        if (!Objects.equals(userId, conversation.getUserId())) {
            throw new ArtException(403, "无权访问该对话");
        }
        return conversation;
    }

    /**
     * 构建prompt
     *
     * @param memoryKey 对话记忆key
     * @param chatId    对话id
     * @param question  输入问题
     * @param kbId      知识库id
     * @return prompt
     */
    private MemoryPrompt buildPrompt(String memoryKey, String chatId, String question, Long kbId) {
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
            attachRagContext(prompt, question, kbId);
        } catch (Exception e) {
            log.warn("RAG知识库查询失败，已降级为普通对话: {}", e.getMessage());
        }
        return prompt;
    }

    /**
     * 查询RAG知识库并将检索内容作为过程消息（不入记忆）附加到提示词
     *
     * <p>多租户隔离：指定知识库时校验其归属当前租户；未指定时只检索
     * 当前租户名下的知识库与直接向量化的文档，避免跨租户内容泄露。</p>
     *
     * @param prompt   对话提示词
     * @param question 用户问题
     * @param kbId     知识库id
     */
    private void attachRagContext(MemoryPrompt prompt, String question, Long kbId) {
        List<Document> ragDocuments = new ArrayList<>();
        if (kbId != null) {
            // 指定知识库时必须属于当前生效租户
            if (knowledgeBaseMapper.selectOneById(kbId) == null) {
                throw new ArtException("知识库不存在或无权访问");
            }
            ragDocuments.addAll(this.searchRagDocuments(question, wrapper -> wrapper.eq("metadata.kb_id", kbId)));
        } else {
            // 未指定知识库：仅检索当前租户名下的知识库（逐个知识库过滤，避免跨租户检索）
            for (Long tenantKbId : this.currentTenantKnowledgeBaseIds()) {
                ragDocuments.addAll(this.searchRagDocuments(question, wrapper -> wrapper.eq("metadata.kb_id", tenantKbId)));
            }
        }
        // 多路检索结果按文档ID去重
        ragDocuments = new ArrayList<>(ragDocuments.stream()
                .filter(document -> document.getId() != null)
                .collect(Collectors.toMap(Document::getId, document -> document, (a, b) -> a, LinkedHashMap::new))
                .values());
        // 重排模型优化精确度
        if (CollectionUtil.isNotEmpty(ragDocuments)) {
            if (rerankModel != null) {
                ragDocuments = rerankModel.rerank(question, ragDocuments);
            }
            // 仅取相似度最高的前5条作为上下文
            ragDocuments = new ArrayList<>(ragDocuments.subList(0, Math.min(ragDocuments.size(), 5)));
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
     * 执行一次RAG向量检索
     *
     * @param question 用户问题
     * @param filter   元数据过滤条件
     * @return 检索结果
     */
    private List<Document> searchRagDocuments(String question, java.util.function.Consumer<SearchWrapper> filter) {
        SearchWrapper wrapper = new SearchWrapper()
                .text(question).maxResults(20).minScore(0.2).outputVector(true);
        filter.accept(wrapper);
        return vectorStore.search(wrapper);
    }

    /**
     * 查询当前生效租户名下的知识库ID集合
     *
     * @return 知识库ID集合
     */
    private List<Long> currentTenantKnowledgeBaseIds() {
        return knowledgeBaseMapper.selectListByQuery(QueryWrapper.create())
                .stream()
                .map(KnowledgeBase::getId)
                .filter(Objects::nonNull)
                .toList();
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

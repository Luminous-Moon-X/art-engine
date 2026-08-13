package com.art.service;

import com.art.domain.vo.ChatMessageVO;
import com.art.domain.vo.ConversationVO;
import com.art.domain.vo.UserChatVO;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI对话服务
 *
 * @author Luminous.X
 * @since 1.3.0
 */
public interface AIChatService {
    /**
     * AI对话
     *
     * @param userChatVO 用户对话对象
     * @return SseEmitter 流式响应
     */
    SseEmitter chat(UserChatVO userChatVO);

    /**
     * 查询当前登录用户的所有对话
     *
     * @return 对话列表（按创建时间倒序）
     */
    List<ConversationVO> listConversations();

    /**
     * 查询指定对话的全部消息内容
     *
     * @param conversationId 对话ID
     * @return 消息列表（按时间正序）
     */
    List<ChatMessageVO> listMessages(String conversationId);

    /**
     * 向量化处理文档
     *
     * @param file 文档
     */
    void vectorDoc(MultipartFile file);
}

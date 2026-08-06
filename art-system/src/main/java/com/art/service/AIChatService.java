package com.art.service;

import com.art.domain.vo.UserChatVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
}

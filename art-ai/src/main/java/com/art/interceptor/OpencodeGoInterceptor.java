package com.art.interceptor;

import com.agentsflex.core.model.chat.*;
import com.agentsflex.core.model.chat.response.AiMessageResponse;
import com.agentsflex.core.model.client.ChatRequestSpec;

/**
 * Opencode Go拦截器<br>
 * 用于解决Opencode Go强制要求请求中携带x-opencode-session header的问题
 *
 * @author Luminous.X
 * @since 2.0.0
 */
public class OpencodeGoInterceptor implements ChatInterceptor {
    /**
     * 拦截流式请求
     *
     * @param chatModel AI模型
     * @param context   上下文
     * @param listener  监听器
     * @param chain     流式请求链
     */
    @Override
    public void interceptStream(BaseChatModel<?> chatModel, ChatContext context, StreamResponseListener listener, StreamChain chain) {
        Object conversationId = context.getConversationId();
        ChatRequestSpec spec = context.getRequestSpec();
        if (spec != null) {
            spec.addHeader("x-opencode-session", String.valueOf(conversationId));
        }
        chain.proceed(chatModel, context, listener);
    }

    /**
     * 拦截同步请求
     *
     * @param chatModel AI模型
     * @param context   上下文
     * @param chain     同步请求链
     * @return 响应
     */
    @Override
    public AiMessageResponse intercept(BaseChatModel<?> chatModel, ChatContext context, SyncChain chain) {
        Object conversationId = context.getConversationId();
        ChatRequestSpec spec = context.getRequestSpec();
        if (spec != null) {
            spec.addHeader("x-opencode-session", String.valueOf(conversationId));
        }
        return chain.proceed(chatModel, context);
    }
}

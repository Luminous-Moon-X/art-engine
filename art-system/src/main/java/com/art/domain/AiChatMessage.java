package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI对话内容表实体类
 *
 * <p>存储用户在一个对话中的所有对话内容，包括用户发出的消息（USER）
 * 和AI回答的消息（AI），通过对话ID与对话表关联。</p>
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_ai_chat_message")
public class AiChatMessage extends BaseEntity {

    /**
     * 对话ID，关联对话表
     */
    @Column("conversation_id")
    private String conversationId;

    /**
     * 消息类型：USER-用户消息 / AI-AI回答
     */
    @Column("message_type")
    private String messageType;

    /**
     * 消息内容
     */
    @Column("content")
    private String content;

    /**
     * 消息类型常量：用户消息
     */
    public static final String TYPE_USER = "USER";

    /**
     * 消息类型常量：AI回答
     */
    public static final String TYPE_AI = "AI";
}

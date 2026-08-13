package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI对话表实体类
 *
 * <p>存储每个用户创建的对话：对话ID、对话名称、创建时间、对话轮次数等。
 * 新对话只有在用户发出第一条消息并收到AI回答后才会写入该表。</p>
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_ai_conversation")
public class AiConversation extends BaseEntity {

    /**
     * 对话所属用户ID
     */
    @Column("user_id")
    private Long userId;

    /**
     * 对话ID（前端生成的唯一标识）
     */
    @Column("conversation_id")
    private String conversationId;

    /**
     * 对话名称（首轮问答完成后由AI总结生成）
     */
    @Column("conversation_name")
    private String conversationName;

    /**
     * 对话轮次数（一轮 = 一次用户提问 + 一次AI回答）
     */
    @Column("turn_count")
    private Integer turnCount;
}

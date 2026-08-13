package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话信息VO
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Data
public class ConversationVO {
    /**
     * 对话ID
     */
    private String conversationId;
    /**
     * 对话名称
     */
    private String conversationName;
    /**
     * 对话轮次数
     */
    private Integer turnCount;
    /**
     * 对话创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

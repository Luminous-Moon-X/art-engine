package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI对话内容VO
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Data
public class ChatMessageVO {
    /**
     * 消息类型：USER-用户消息 / AI-AI回答
     */
    private String messageType;
    /**
     * 消息内容
     */
    private String content;
    /**
     * 消息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

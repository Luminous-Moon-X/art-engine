package com.art.domain.vo;

import lombok.Data;

/**
 * 用户AI对话VO
 *
 * @author Luminuos.X
 * @since 1.3.0
 */
@Data
public class UserChatVO {
    /**
     * 对话ID
     */
    private String chatId;
    /**
     * 用户问题
     */
    private String question;
    /**
     * 知识库ID
     */
    private Long kbId;
}

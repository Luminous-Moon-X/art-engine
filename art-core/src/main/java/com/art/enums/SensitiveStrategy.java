package com.art.enums;

/**
 * 字段脱敏策略
 *
 * @author Luminous.X
 * @since 1.3.3
 */
public enum SensitiveStrategy {
    /**
     * 全部掩码
     */
    MASK_ALL,
    /**
     * 手机号，如 138****5678
     */
    MASK_PHONE,
    /**
     * 邮箱，如 m***@example.com
     */
    MASK_EMAIL,
    /**
     * 身份证，如 110***********1234
     */
    MASK_ID_CARD,
    /**
     * 银行卡，如 6222 **** **** 1234
     */
    MASK_BANK_CARD,
    /**
     * 通用前后保留
     */
    CUSTOM
}

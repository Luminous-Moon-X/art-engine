package com.art.utils;

import com.art.enums.SensitiveStrategy;

/**
 * 字段脱敏工具类
 *
 * @author Luminous.X
 * @since 1.3.3
 */
public class SensitiveUtil {

    /**
     * 脱敏处理
     *
     * @param value     待脱敏的值
     * @param strategy  脱敏策略
     * @param prefixLen 保留前缀长度
     * @param suffixLen 保留后缀长度
     * @param maskChar  掩码字符
     * @return 脱敏后的值
     */
    public static String mask(String value,
                              SensitiveStrategy strategy,
                              int prefixLen,
                              int suffixLen,
                              String maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        String lastFour = value.substring(value.length() - 4);
        switch (strategy) {
            case MASK_ALL:
                // 固定返回6个*
                return repeat(maskChar, 6);
            case MASK_PHONE:
                if (value.length() == 11) {
                    return value.substring(0, 3) + "****" + value.substring(7);
                }
                break;
            case MASK_EMAIL:
                int at = value.indexOf('@');
                if (at > 1) {
                    return value.charAt(0) + "***" + value.substring(at);
                }
                break;
            case MASK_ID_CARD:
                if (value.length() >= 8) {
                    return value.substring(0, 4) + repeat(maskChar, value.length() - 8)
                            + lastFour;
                }
                break;
            case MASK_BANK_CARD:
                if (value.length() >= 8) {
                    return value.substring(0, 4) + " **** **** " + lastFour;
                }
                break;
            case CUSTOM:
            default:
                break;
        }

        // 通用前后保留策略
        if (prefixLen <= 0 && suffixLen <= 0) {
            return repeat(maskChar, value.length());
        }
        if (value.length() <= prefixLen + suffixLen) {
            return repeat(maskChar, value.length());
        }
        String prefix = value.substring(0, prefixLen);
        String suffix = suffixLen > 0 ? value.substring(value.length() - suffixLen) : "";
        String middle = repeat(maskChar, value.length() - prefixLen - suffixLen);
        return prefix + middle + suffix;
    }

    /**
     * 生成指定长度的掩码字符串
     *
     * @param ch    掩码字符
     * @param count 掩码长度
     * @return 掩码字符串
     */
    private static String repeat(String ch, int count) {
        StringBuilder sb = new StringBuilder();
        sb.repeat(String.valueOf(ch), Math.max(0, count));
        return sb.toString();
    }

}
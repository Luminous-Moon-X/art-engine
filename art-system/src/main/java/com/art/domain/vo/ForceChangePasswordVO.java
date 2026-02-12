package com.art.domain.vo;

import lombok.Data;

/**
 * 强制修改密码请求参数VO
 *
 * @author Luminous.X
 * @since 1.1.2
 */
@Data
public class ForceChangePasswordVO {
    /**
     * 临时token
     */
    private String tempToken;
    
    /**
     * 新密码
     */
    private String newPassword;
    
    /**
     * 确认新密码
     */
    private String confirmPassword;
}
package com.art.domain.vo;

import lombok.Data;

/**
 * 用户重置密码参数
 *
 * @author Luminous.X
 * @since 1.1.2
 */
@Data
public class UserResetPasswordVO {
    /**
     * 旧密码
     */
    private String oldPassword;
    /**
     * 新密码
     */
    private String newPassword;
    /**
     * 确认密码
     */
    private String confirmPassword;
}

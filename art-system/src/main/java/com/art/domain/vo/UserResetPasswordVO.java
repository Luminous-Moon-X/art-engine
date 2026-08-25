package com.art.domain.vo;

import com.art.annotation.Sensitive;
import com.art.enums.SensitiveStrategy;
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
    @Sensitive(strategy = SensitiveStrategy.MASK_ALL)
    private String oldPassword;
    /**
     * 新密码
     */
    @Sensitive(strategy = SensitiveStrategy.MASK_ALL)
    private String newPassword;
    /**
     * 确认密码
     */
    @Sensitive(strategy = SensitiveStrategy.MASK_ALL)
    private String confirmPassword;
}

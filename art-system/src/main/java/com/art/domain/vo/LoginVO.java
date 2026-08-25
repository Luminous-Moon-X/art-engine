package com.art.domain.vo;

import com.art.annotation.Sensitive;
import com.art.enums.SensitiveStrategy;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 登录表单请求VO
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class LoginVO {
    /**
     * 用户名
     */
    @JsonProperty("userName")
    private String username;
    /**
     * 密码
     */
    @JsonProperty("password")
    @Sensitive(strategy = SensitiveStrategy.MASK_ALL)
    private String password;
    /**
     * 租户id
     */
    @JsonProperty("tenantId")
    private String tenantId;
    /**
     * 验证码
     */
    @JsonProperty("captcha")
    private String captcha;

}

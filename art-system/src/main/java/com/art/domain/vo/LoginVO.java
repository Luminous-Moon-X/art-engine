package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 登录表单请求VO
 *
 * @author Luminous.X
 * @since 0.0.1
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

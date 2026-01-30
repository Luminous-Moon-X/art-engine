package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 登出注销VO
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class LogoutVO {
    /**
     * 需要登出注销的token
     */
    @JsonProperty("TOKEN")
    private String token;

    /**
     * 用户名
     */
    @JsonProperty("USER_NAME")
    private String userName;
}

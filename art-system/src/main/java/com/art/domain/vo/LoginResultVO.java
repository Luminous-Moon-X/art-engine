package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录接口返回值VO
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
@AllArgsConstructor
public class LoginResultVO {
    @JsonProperty("token")
    private String token;
}

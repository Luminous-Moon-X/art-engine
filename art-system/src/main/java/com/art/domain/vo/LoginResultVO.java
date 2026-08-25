package com.art.domain.vo;

import com.art.annotation.Sensitive;
import com.art.enums.SensitiveStrategy;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录接口返回值VO
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResultVO {
    /**
     * token
     */
    @JsonProperty("token")
    @Sensitive(strategy = SensitiveStrategy.MASK_ALL)
    private String token;
    /**
     * 强制修改密码
     */
    private Boolean forceChangePassword = false;
}

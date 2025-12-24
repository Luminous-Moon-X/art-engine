package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 用户信息VO
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class UserInfoVO {
    @JsonProperty("buttons")
    private String[] buttons;

    @JsonProperty("email")
    private String email;

    @JsonProperty("roles")
    private String[] roles;

    @JsonProperty("userId")
    private Long userId;

    @JsonProperty("userName")
    private String userName;
}

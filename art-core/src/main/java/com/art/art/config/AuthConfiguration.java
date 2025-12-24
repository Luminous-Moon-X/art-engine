package com.art.art.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 权限配置
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "art.auth")
public class AuthConfiguration {
    /**
     * token过期时间
     */
    private Integer tokenExpireTime;
}

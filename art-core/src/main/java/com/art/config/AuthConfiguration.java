package com.art.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限配置
 *
 * @author Luminous.X
 * @since 1.0.0
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
    /**
     * 密钥
     */
    private String secretKey;
    /**
     * 请求白名单
     */
    private List<String> whiteList;
}

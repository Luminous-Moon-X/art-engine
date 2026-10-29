package com.art.properties;

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
public class AuthProperties {
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
    /**
     * 会话续期节流间隔（秒）：同一 Token 在该时间内最多触发一次 Sa-Token 会话续期<br/>
     * 默认 300 秒，远小于 token 过期时间，不会使会话提前失效；配置为 0 或负数表示每个请求都续期
     */
    private Long sessionRenewIntervalSeconds = 300L;
}

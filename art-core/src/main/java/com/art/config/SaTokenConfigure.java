package com.art.config;

import cn.dev33.satoken.config.SaTokenConfig;
import com.art.properties.AuthProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

/**
 * SaToken注入JWT实现
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Configuration
@DependsOn("authProperties")
public class SaTokenConfigure {

    /**
     * 自定义权限配置
     */
    private final AuthProperties authProperties;

    /**
     * 构造函数
     *
     * @param authProperties 自定义权限配置
     */
    public SaTokenConfigure(AuthProperties authProperties) {
        this.authProperties = authProperties;
    }

    /**
     * jwt Simple简单模式
     *
     * @return StpLogic
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    /**
     * 自定义sa-token配置
     *
     * @return SaTokenConfig sa-token配置
     */
    @Bean
    @Primary
    public SaTokenConfig saTokenConfig() {
        SaTokenConfig config = new SaTokenConfig();
        config.setJwtSecretKey(authProperties.getSecretKey());
        config.setIsReadCookie(false);
        config.setTimeout(authProperties.getTokenExpireTime() * 60L);
        return config;
    }
}

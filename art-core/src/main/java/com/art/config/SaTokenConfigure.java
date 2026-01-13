package com.art.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpLogic;

/**
 * SaToken注入JWT实现
 * 
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
public class SaTokenConfigure {
    /**
     * Simple简单模式
     * 
     * @return StpLogic
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }
}

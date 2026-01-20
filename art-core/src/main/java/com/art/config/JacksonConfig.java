package com.art.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * Jackson自定义配置
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
public class JacksonConfig {
    /**
     * 日志记录
     */
    private static final Logger log = LoggerFactory.getLogger(JacksonConfig.class);

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonSetting() {
        return builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(Long.class, JacksonLongConfiguration.INSTANCE);
            javaTimeModule.addSerializer(Long.TYPE, JacksonLongConfiguration.INSTANCE);
            builder.modules(javaTimeModule);
            builder.timeZone(TimeZone.getDefault());
            log.info("Jackson配置初始化成功.");
        };
    }
}

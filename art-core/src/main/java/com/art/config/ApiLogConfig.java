package com.art.config;

import com.art.SensitiveLogModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * API日志配置
 *
 * @author Luminous.X
 * @since 1.3.3
 */
@Configuration
public class ApiLogConfig {

    /**
     * 配置API日志的ObjectMapper
     *
     * @return ObjectMapper
     */
    @Bean("apiLogObjectMapper")
    public ObjectMapper apiLogObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册数据脱敏模块
        mapper.registerModule(new SensitiveLogModule());
        // 关闭 FAIL_ON_EMPTY_BEANS，避免部分对象序列化异常
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }
}
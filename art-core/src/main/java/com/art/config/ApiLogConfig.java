package com.art.config;

import com.art.SensitiveLogModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    public static ObjectMapper apiLogObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册数据脱敏模块
        mapper.registerModule(new SensitiveLogModule());
        // 注册时间序列化模块，并将 LocalDateTime 按 yyyy-MM-dd HH:mm:ss 输出/解析
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        mapper.registerModule(javaTimeModule);
        // 关闭日期时间戳输出（例如 LocalDateTime -> [2026,8,25,15,39,22,...]）
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 关闭 FAIL_ON_EMPTY_BEANS，避免部分对象序列化异常
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }
}
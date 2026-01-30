package com.art.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Jackson自定义配置
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Configuration
public class JacksonConfig {
    /**
     * 日志记录
     */
    private static final Logger log = LoggerFactory.getLogger(JacksonConfig.class);
    /**
     * 时间格式化
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonSetting() {
        return builder -> {
            JavaTimeModule javaTimeModule = new JavaTimeModule();
            javaTimeModule.addSerializer(Long.class, JacksonLongConfiguration.INSTANCE);
            javaTimeModule.addSerializer(Long.TYPE, JacksonLongConfiguration.INSTANCE);
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
            builder.modules(javaTimeModule);
            builder.timeZone(TimeZone.getDefault());
            builder.dateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            log.info("Jackson配置初始化成功.");
        };
    }
}

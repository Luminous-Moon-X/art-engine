package com.art.log.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 日志模块自动配置类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Configuration
@EnableAsync
@ComponentScan("com.art.log")
public class LogAutoConfiguration {
}

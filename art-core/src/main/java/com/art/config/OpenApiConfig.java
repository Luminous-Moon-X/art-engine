package com.art.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 配置类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
public class OpenApiConfig {

    /**
     * 创建 OpenAPI 对象并配置信息
     *
     * @return OpenAPI 对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Art Engine api接口文档")
                        .version("1.0.0")
                        .description("基于 Java 21 和 Spring Boot 3 构建的后端接口服务")
                        .license(new License().name("Gitee-Art Engine").url("https://gitee.com/hiroshi-xh/art-engine")));
    }
}
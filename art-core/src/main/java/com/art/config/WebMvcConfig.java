package com.art.config;

import com.art.interceptor.RequestHeaderInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 拦截器
     */
    private final RequestHeaderInterceptor requestHeaderInterceptor;

    /**
     * 认证配置
     */
    private final AuthConfiguration authConfiguration;

    /**
     * 添加拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器，应用到所有路径
        registry.addInterceptor(requestHeaderInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(authConfiguration.getWhiteList());
        log.info("初始化拦截器和请求白名单成功");
    }
}

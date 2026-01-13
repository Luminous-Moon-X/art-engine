package com.art.config;

import com.art.interceptor.RequestHeaderInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册拦截器
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 拦截器
     */
    private final RequestHeaderInterceptor requestHeaderInterceptor;

    /**
     * 添加拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册拦截器，应用到所有路径
        registry.addInterceptor(requestHeaderInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns("/swagger-ui/**")
                .excludePathPatterns("/api-docs/**")
                .excludePathPatterns("/swagger-ui.html")
                .excludePathPatterns("/auth/login")  // 但排除登录接口
                .excludePathPatterns("/auth/logout"); // 以及登出接口
    }
}

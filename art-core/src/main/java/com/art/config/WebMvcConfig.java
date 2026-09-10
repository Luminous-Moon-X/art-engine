package com.art.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import com.art.interceptor.RequestHeaderInterceptor;
import com.art.properties.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

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
    private final AuthProperties authProperties;

    /**
     * 添加拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> whiteList = authProperties.getWhiteList();
        // 注册拦截器，应用到所有路径
        registry.addInterceptor(requestHeaderInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(whiteList);
        registry.addInterceptor(new SaInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(whiteList);
        log.info("初始化拦截器和请求白名单成功");
    }
}

package com.art.interceptor;

import com.art.common.LoginUser;
import com.art.config.AuthConfiguration;
import com.art.context.SecurityContextHolder;
import com.art.exception.ArtException;
import com.art.constants.ArtErrorMessageConstants;
import com.art.constants.HeaderKeyConstants;
import com.art.kits.StringUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * 请求统一拦截器
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Component
@SuppressWarnings("unused")
public class RequestHeaderInterceptor implements HandlerInterceptor {
    /**
     * Redis客户端
     */
    private final RedisTemplate<String, String> redisTemplate;
    /**
     * Token有效期配置
     */
    private final AuthConfiguration authConfiguration;

    /**
     * 构造器注入
     *
     * @param redisTemplate     Redis客户端
     * @param authConfiguration Token有效期配置
     */
    public RequestHeaderInterceptor(RedisTemplate<String, String> redisTemplate, AuthConfiguration authConfiguration) {
        this.redisTemplate = redisTemplate;
        this.authConfiguration = authConfiguration;
    }

    /**
     * 请求前置处理
     *
     * @param request  请求头
     * @param response 响应头
     * @param handler  处理对象
     * @return 是否通过
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 请求头中获取token
        String token = request.getHeader(HeaderKeyConstants.TOKEN_HEADER);
        // 判断token是否合法
        if (StringUtil.isBlank(token)) {
            throw new ArtException(401, ArtErrorMessageConstants.USER_STATUS_EXPIRE);
        }
        if (token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
        }
        if (!verifyToken(token)) {
            throw new ArtException(401, ArtErrorMessageConstants.USER_STATUS_EXPIRE);
        }
        // 根据token获取当前用户信息，并将用户信息存储到线程变量中
        String tokenKey = "access_token:" + token;
        // Token续期
        redisTemplate.expire(tokenKey, Duration.ofMinutes(authConfiguration.getTokenExpireTime()));
        LoginUser loginUser = JSON.parseObject(redisTemplate.opsForValue().get(tokenKey), LoginUser.class);
        if (loginUser == null) {
            throw new ArtException(401, ArtErrorMessageConstants.USER_STATUS_EXPIRE);
        }
        // 设置线程变量
        SecurityContextHolder.setUserId(loginUser.getUserId());
        SecurityContextHolder.setUserName(loginUser.getUserName());
        SecurityContextHolder.setUserAllName(loginUser.getUserAllName());
        SecurityContextHolder.setUserType(loginUser.getUserType());
        SecurityContextHolder.setToken(token);
        return true;
    }

    /**
     * 判断该token是否合法
     *
     * @param token token
     * @return token是否合法
     */
    private boolean verifyToken(String token) {
        return redisTemplate.hasKey("access_token:" + token);
    }
}

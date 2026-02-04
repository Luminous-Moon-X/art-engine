package com.art.interceptor;

import com.art.cache.UserRoleCache;
import com.art.common.LoginUser;
import com.art.config.AuthConfiguration;
import com.art.context.SecurityContextHolder;
import com.art.exception.ArtException;
import com.art.constants.ArtErrorMessageConstants;
import com.art.constants.HeaderKeyConstants;
import com.art.utils.StringUtil;
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
 * @since 1.0.0
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

    private final UserRoleCache userRoleCache;

    /**
     * 构造器注入
     *
     * @param redisTemplate     Redis客户端
     * @param authConfiguration Token有效期配置
     * @param userRoleCache     用户角色缓存
     */
    public RequestHeaderInterceptor(RedisTemplate<String, String> redisTemplate, AuthConfiguration authConfiguration, UserRoleCache userRoleCache) {
        this.redisTemplate = redisTemplate;
        this.authConfiguration = authConfiguration;
        this.userRoleCache = userRoleCache;
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

        // 根据token获取当前用户信息，并将用户信息存储到线程变量中
        String tokenKey = "access_token:" + token;

        // 优化：直接获取用户信息，如果为空则说明Token无效或已过期
        String userInfoJson = redisTemplate.opsForValue().get(tokenKey);
        if (StringUtil.isBlank(userInfoJson)) {
            throw new ArtException(401, ArtErrorMessageConstants.USER_STATUS_EXPIRE);
        }

        // Token续期
        redisTemplate.expire(tokenKey, Duration.ofMinutes(authConfiguration.getTokenExpireTime()));

        LoginUser loginUser = JSON.parseObject(userInfoJson, LoginUser.class);
        if (loginUser == null) {
            throw new ArtException(401, ArtErrorMessageConstants.USER_STATUS_EXPIRE);
        }
        // 设置线程变量
        SecurityContextHolder.setUserId(loginUser.getId());
        SecurityContextHolder.setUserName(loginUser.getUserName());
        SecurityContextHolder.setUserAllName(loginUser.getNickName());
        SecurityContextHolder.setDeptId(loginUser.getDeptId());
        SecurityContextHolder.setUserType(loginUser.getUserType());
        SecurityContextHolder.setRoleIds(userRoleCache.getRoleByUserId(loginUser.getId()));
        SecurityContextHolder.setToken(token);
        return true;
    }
}

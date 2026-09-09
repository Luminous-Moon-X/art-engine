package com.art.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.art.auth.cache.RoleCache;
import com.art.auth.cache.UserRoleCache;
import com.art.common.LoginUser;
import com.art.config.AuthConfiguration;
import com.art.constants.TenantConstants;
import com.art.context.SecurityContextHolder;
import com.art.exception.ArtException;
import com.art.constants.ArtErrorMessageConstants;
import com.art.constants.HeaderKeyConstants;
import com.art.tenant.TenantStatusProvider;
import com.art.tenant.TenantSupport;
import com.art.utils.StringUtil;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.List;

/**
 * 请求统一拦截器
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
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
     * 用户角色缓存
     */
    private final UserRoleCache userRoleCache;
    /**
     * 角色缓存
     */
    private final RoleCache roleCache;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;
    /**
     * 租户状态提供者（校验租户是否启用/未到期）
     */
    private final TenantStatusProvider tenantStatusProvider;

    /**
     * 构造器注入
     *
     * @param redisTemplate        Redis客户端
     * @param authConfiguration    Token有效期配置
     * @param userRoleCache        用户角色缓存
     * @param roleCache            角色缓存
     * @param tenantSupport        多租户支持
     * @param tenantStatusProvider 租户状态提供者
     */
    public RequestHeaderInterceptor(RedisTemplate<String, String> redisTemplate, AuthConfiguration authConfiguration,
                                    UserRoleCache userRoleCache, RoleCache roleCache, TenantSupport tenantSupport,
                                    TenantStatusProvider tenantStatusProvider) {
        this.redisTemplate = redisTemplate;
        this.authConfiguration = authConfiguration;
        this.userRoleCache = userRoleCache;
        this.roleCache = roleCache;
        this.tenantSupport = tenantSupport;
        this.tenantStatusProvider = tenantStatusProvider;
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
        if ("/api/error".equals(request.getRequestURI())) {
            return true;
        }
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
        StpUtil.renewTimeout(authConfiguration.getTokenExpireTime() * 60L);

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
        Long tenantId = this.resolveTenantId(token, loginUser);
        // 多租户开启时，每个请求校验生效租户是否仍可用（禁用/到期立即失效）
        this.validateTenantStatus(tenantId);
        SecurityContextHolder.setTenantId(tenantId);
        List<Long> roleIds = userRoleCache.getRoleByUserId(loginUser.getId());
        SecurityContextHolder.setRoleIds(roleIds);
        SecurityContextHolder.setRoleCodes(this.roleCache.getRoleCodeByIds(roleIds));
        SecurityContextHolder.setToken(token);
        return true;
    }

    /**
     * 解析当前请求生效的租户ID<br/>
     * 超级管理员登录后切换租户时会写入 tenant_context:{token}，优先使用该值；
     * 其他情况使用用户自身归属的租户ID
     *
     * @param token      Token
     * @param loginUser  当前登录用户
     * @return 生效租户ID
     */
    private Long resolveTenantId(String token, LoginUser loginUser) {
        Long sessionTenantId = null;
        try {
            String tenantContextKey = TenantConstants.TENANT_CONTEXT_KEY_PREFIX + token;
            String tenantContext = redisTemplate.opsForValue().get(tenantContextKey);
            if (StringUtil.isNotBlank(tenantContext)) {
                sessionTenantId = Long.parseLong(tenantContext.trim());
                // 与登录态同步续期，避免上下文键在会话仍有效时提前过期或长期残留
                redisTemplate.expire(tenantContextKey, Duration.ofMinutes(authConfiguration.getTokenExpireTime()));
            }
        } catch (NumberFormatException ignored) {
            // 租户上下文非法时回退到用户自身租户
        }
        return sessionTenantId != null ? sessionTenantId : loginUser.getTenantId();
    }

    /**
     * 校验当前生效租户是否可用（启用且未到期）
     *
     * <p>多租户关闭时不校验；租户记录不存在时放行并打印告警，
     * 避免因历史数据缺少租户行而整体不可用。</p>
     *
     * @param tenantId 生效租户ID
     */
    private void validateTenantStatus(Long tenantId) {
        if (!tenantSupport.isEnable() || tenantId == null) {
            return;
        }
        TenantStatusProvider.TenantStatus status = tenantStatusProvider.getStatus(tenantId);
        if (status == null) {
            log.warn("当前会话租户不存在，跳过租户状态校验：tenantId={}", tenantId);
            return;
        }
        if (status.isDisabled()) {
            throw new ArtException(401, "租户不存在或已禁用，请重新登录！");
        }
        if (status.isExpired()) {
            throw new ArtException(401, "租户已到期，请联系管理员！");
        }
    }
}

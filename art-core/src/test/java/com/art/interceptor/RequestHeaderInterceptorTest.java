package com.art.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.art.auth.cache.RoleCache;
import com.art.auth.cache.UserRoleCache;
import com.art.common.LoginUser;
import com.art.context.SecurityContextHolder;
import com.art.exception.ArtException;
import com.art.properties.AuthProperties;
import com.art.tenant.TenantStatusProvider;
import com.art.tenant.TenantSupport;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * 请求拦截器测试
 *
 * <p>重点固化两点：</p>
 * <ul>
 *     <li>登录态与租户上下文由一次 pipeline 返回，且结果下标映射正确（0=登录用户，2=租户上下文）；</li>
 *     <li>Sa-Token 会话续期按配置间隔节流，不会每个请求都触发。</li>
 * </ul>
 *
 * @author Luminous.X
 * @since 1.3.1
 */
class RequestHeaderInterceptorTest {

    /**
     * 测试用Token
     */
    private static final String TOKEN = "test-token-value";

    /**
     * Redis客户端
     */
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 权限配置
     */
    private AuthProperties authProperties;

    /**
     * 用户角色缓存
     */
    private UserRoleCache userRoleCache;

    /**
     * 角色缓存
     */
    private RoleCache roleCache;

    /**
     * 多租户支持
     */
    private TenantSupport tenantSupport;

    /**
     * 拦截器
     */
    private RequestHeaderInterceptor interceptor;

    /**
     * 初始化测试环境
     */
    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        this.redisTemplate = mock(RedisTemplate.class);
        this.authProperties = new AuthProperties();
        this.authProperties.setTokenExpireTime(60);
        this.authProperties.setSessionRenewIntervalSeconds(300L);
        this.userRoleCache = mock(UserRoleCache.class);
        this.roleCache = mock(RoleCache.class);
        this.tenantSupport = mock(TenantSupport.class);
        this.interceptor = new RequestHeaderInterceptor(this.redisTemplate, this.authProperties,
                this.userRoleCache, this.roleCache, this.tenantSupport, mock(TenantStatusProvider.class));
        when(this.tenantSupport.isEnable()).thenReturn(false);
    }

    /**
     * 清理线程变量
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    /**
     * 一次 pipeline 同时取回登录用户与租户上下文，并将其写入线程变量
     */
    @Test
    @DisplayName("pipeline 结果按下标解析：0=登录用户，2=租户上下文")
    void shouldReadUserAndTenantContextFromSinglePipeline() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        loginUser.setUserName("tester");
        loginUser.setNickName("测试用户");
        loginUser.setDeptId(2L);
        loginUser.setUserType("user");
        loginUser.setTenantId(7L);
        String userJson = JSON.toJSONString(loginUser);
        // 模拟 pipeline 返回：GET 登录态、EXPIRE、GET 租户上下文、EXPIRE
        doReturn(Arrays.asList(userJson, Boolean.TRUE, "42", Boolean.FALSE))
                .when(this.redisTemplate).executePipelined(any(RedisCallback.class));
        when(this.userRoleCache.getRoleByUserId(1L)).thenReturn(List.of(10L));
        when(this.roleCache.getRoleCodeByIds(List.of(10L))).thenReturn(List.of("admin"));

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            assertThat(this.interceptor.preHandle(requestWithToken("Bearer " + TOKEN),
                    new MockHttpServletResponse(), new Object())).isTrue();

            assertThat(SecurityContextHolder.getUserId()).isEqualTo(1L);
            // 租户上下文（下标 2）优先于用户自身租户（7）
            assertThat(SecurityContextHolder.getTenantId()).isEqualTo(42L);
            assertThat(SecurityContextHolder.getRoleIds()).containsExactly(10L);
            assertThat(SecurityContextHolder.getRoleCodes()).containsExactly("admin");
            assertThat(SecurityContextHolder.getToken()).isEqualTo(TOKEN);
            stpUtil.verify(() -> StpUtil.renewTimeout(anyLong()), times(1));
        }
    }

    /**
     * 租户上下文非法时回退到用户自身租户
     */
    @Test
    @DisplayName("租户上下文非法时回退到用户自身租户")
    void shouldFallbackToUserTenantWhenTenantContextInvalid() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        loginUser.setTenantId(7L);
        String userJson = JSON.toJSONString(loginUser);
        doReturn(Arrays.asList(userJson, Boolean.TRUE, "not-a-number", Boolean.FALSE))
                .when(this.redisTemplate).executePipelined(any(RedisCallback.class));
        when(this.userRoleCache.getRoleByUserId(1L)).thenReturn(List.of());
        when(this.roleCache.getRoleCodeByIds(List.of())).thenReturn(List.of());

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            assertThat(this.interceptor.preHandle(requestWithToken(TOKEN),
                    new MockHttpServletResponse(), new Object())).isTrue();
            assertThat(SecurityContextHolder.getTenantId()).isEqualTo(7L);
        }
    }

    /**
     * 登录态不存在时返回 401
     */
    @Test
    @DisplayName("登录态不存在时返回 401")
    void shouldRejectWhenSessionMissing() {
        doReturn(Arrays.asList(null, Boolean.FALSE, null, Boolean.FALSE))
                .when(this.redisTemplate).executePipelined(any(RedisCallback.class));

        assertThatThrownBy(() -> this.interceptor.preHandle(requestWithToken(TOKEN),
                new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ArtException.class)
                .extracting("errorCode").isEqualTo(401);
    }

    /**
     * 未携带Token时返回 401（不访问Redis）
     */
    @Test
    @DisplayName("未携带Token时返回 401")
    void shouldRejectWhenTokenBlank() {
        assertThatThrownBy(() -> this.interceptor.preHandle(new MockHttpServletRequest(),
                new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ArtException.class)
                .extracting("errorCode").isEqualTo(401);
    }

    /**
     * 会话续期按配置间隔节流：间隔内多次请求只续期一次
     */
    @Test
    @DisplayName("会话续期节流：间隔内多次请求只续期一次")
    void shouldThrottleSessionRenew() {
        LoginUser loginUser = new LoginUser();
        loginUser.setId(1L);
        loginUser.setTenantId(7L);
        String userJson = JSON.toJSONString(loginUser);
        doReturn(Arrays.asList(userJson, Boolean.TRUE, null, Boolean.FALSE))
                .when(this.redisTemplate).executePipelined(any(RedisCallback.class));
        when(this.userRoleCache.getRoleByUserId(1L)).thenReturn(List.of());
        when(this.roleCache.getRoleCodeByIds(List.of())).thenReturn(List.of());

        try (MockedStatic<StpUtil> stpUtil = mockStatic(StpUtil.class)) {
            this.interceptor.preHandle(requestWithToken(TOKEN), new MockHttpServletResponse(), new Object());
            this.interceptor.preHandle(requestWithToken(TOKEN), new MockHttpServletResponse(), new Object());
            this.interceptor.preHandle(requestWithToken(TOKEN), new MockHttpServletResponse(), new Object());
            stpUtil.verify(() -> StpUtil.renewTimeout(anyLong()), times(1));
        }
    }

    /**
     * 构造携带Token的请求
     *
     * @param token Token（可带 Bearer 前缀）
     * @return 请求对象
     */
    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/role/page");
        request.addHeader("Authorization", token);
        return request;
    }
}

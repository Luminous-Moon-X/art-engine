package com.art.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.alibaba.fastjson2.JSON;
import com.art.properties.AuthProperties;
import com.art.constants.TenantConstants;
import com.art.domain.Tenant;
import com.art.domain.User;
import com.art.domain.vo.LoginResultVO;
import com.art.domain.vo.LoginVO;
import com.art.domain.vo.UserResetPasswordVO;
import com.art.exception.ArtException;
import com.art.event.LoginLogEvent;
import com.art.service.AuthService;
import com.art.service.TenantService;
import com.art.service.UserService;
import com.art.tenant.TenantSupport;
import com.art.utils.SecurityUtil;
import com.art.utils.StringUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.CollectionUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务接口
 *
 * @author Luminous.X
 * @since 1.1.2
 */
@Service
public class AuthServiceImpl implements AuthService {
    /**
     * 用户表Service层逻辑
     */
    private final UserService userService;
    /**
     * Redis操作对象
     */
    private final RedisTemplate<String, String> redisTemplate;
    /**
     * 权限配置
     */
    private final AuthProperties authProperties;
    /**
     * 事件发布器
     */
    private final ApplicationEventPublisher eventPublisher;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;
    /**
     * 租户服务
     */
    private final TenantService tenantService;

    /**
     * 构造器注入
     *
     * @param userService       用户表Service层逻辑
     * @param redisTemplate     Redis操作对象
     * @param authProperties 权限配置
     * @param eventPublisher    事件发布器
     * @param tenantSupport     多租户支持
     * @param tenantService     租户服务
     */
    public AuthServiceImpl(UserService userService, RedisTemplate<String, String> redisTemplate,
                           AuthProperties authProperties, ApplicationEventPublisher eventPublisher,
                           TenantSupport tenantSupport, TenantService tenantService) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.authProperties = authProperties;
        this.eventPublisher = eventPublisher;
        this.tenantSupport = tenantSupport;
        this.tenantService = tenantService;
    }

    /**
     * 登录接口
     *
     * @param loginVO 登录参数
     * @param request HTTP请求对象
     * @return 登录结果
     */
    @Override
    public LoginResultVO login(LoginVO loginVO, HttpServletRequest request) {
        // 获取用户登录的账号和密码
        String username = loginVO.getUsername();
        String password = loginVO.getPassword();
        if (StringUtils.isAnyBlank(username, password)) {
            throw new ArtException("用户名或密码不能为空！");
        }
        // 多租户开启：校验并定位租户
        Long loginTenantId = this.resolveLoginTenantId(loginVO);
        // 查询用户（系统级作用域：登录线程无租户上下文，按用户名全量匹配后内存定位租户）
        List<User> userList = tenantSupport.systemScope(() -> userService.list(
                QueryWrapper.create().eq(User::getUserName, username)));
        if (CollectionUtil.isEmpty(userList)) {
            throw new ArtException("用户名或密码错误！");
        }
        // 多租户开启：用户必须属于所选租户；超级管理员允许登录任意启用租户（登录即进入该租户上下文）
        User user;
        if (tenantSupport.isEnable()) {
            user = userList.stream()
                    .filter(u -> loginTenantId != null && loginTenantId.equals(u.getTenantId()))
                    .findFirst()
                    .orElse(null);
            if (user == null && userList.size() == 1 && tenantSupport.isSuperAdmin(userList.getFirst().getUserType())) {
                user = userList.getFirst();
            }
            if (user == null) {
                throw new ArtException("用户名或密码错误！");
            }
        } else {
            user = userList.getFirst();
        }
        if (user.getEnableFlag() == 0) {
            throw new ArtException("该用户已被禁用，请联系管理员！");
        }
        // 对比密码
        String realPassword = user.getPassword();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, realPassword)) {
            throw new ArtException("用户名或密码错误！");
        }
        
        // 检查是否首次登录
        boolean isFirstLogin = user.getFirstLoginFlag() != null && user.getFirstLoginFlag() == 1;
        
        LoginResultVO loginResultVO = new LoginResultVO();
        
        if (isFirstLogin) {
            // 生成临时token用于强制修改密码
            String tempToken = "temp_" + UUID.randomUUID().toString().replace("-", "");
            
            // 将临时token存储到Redis，设置较短的过期时间，并标记为一次性使用
            String tempTokenKey = "temp_token:" + tempToken;
            redisTemplate.opsForValue().set(tempTokenKey, JSON.toJSONString(user), 10, TimeUnit.MINUTES);
            
            // 设置强制修改密码标志
            loginResultVO.setForceChangePassword(true);
            loginResultVO.setToken(tempToken);
        } else {
            // 正常登录流程
            StpUtil.login(user.getId(),
                    new SaLoginParameter().setExtra("name", user.getUserName()));
            SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
            String token = tokenInfo.getTokenValue();
            // 将token存储到Redis
            redisTemplate.opsForValue().set("access_token:" + token, JSON.toJSONString(user),
                    authProperties.getTokenExpireTime(), TimeUnit.MINUTES);
            // 初始化会话生效租户（超级管理员登录时写入，可随后切换）
            this.initSessionTenant(token, user, loginTenantId);
            loginResultVO.setToken(token);
        }

        // 发布登录日志事件（携带生效租户，保证日志在多租户下可归属可查询）
        Long logTenantId = loginTenantId != null ? loginTenantId : user.getTenantId();
        eventPublisher.publishEvent(new LoginLogEvent(user.getUserName(), user.getNickName(), request, logTenantId));

        return loginResultVO;
    }

    /**
     * 解析登录时选择的租户ID<br/>
     * 多租户开启：必须携带有效的启用租户；关闭：返回null，不限定租户登录
     *
     * @param loginVO 登录参数
     * @return 租户ID（多租户关闭时为null）
     */
    private Long resolveLoginTenantId(LoginVO loginVO) {
        if (!tenantSupport.isEnable()) {
            return null;
        }
        if (StringUtil.isBlank(loginVO.getTenantId())) {
            throw new ArtException("请选择租户！");
        }
        Long tenantId;
        try {
            tenantId = Long.parseLong(loginVO.getTenantId().trim());
        } catch (NumberFormatException e) {
            throw new ArtException("租户参数错误，请重新选择租户！");
        }
        Tenant tenant = tenantService.selectEnabledById(tenantId);
        if (tenant == null) {
            throw new ArtException("租户不存在或已禁用！");
        }
        if (tenant.getExpireDate() != null && tenant.getExpireDate().isBefore(java.time.LocalDate.now())) {
            throw new ArtException("租户已到期，请联系管理员！");
        }
        return tenantId;
    }

    /**
     * 初始化登录会话的生效租户<br/>
     * 仅超级管理员写入租户上下文（后续可切换租户）；租户管理员/普通用户
     * 始终使用自身归属租户
     *
     * @param token         Token
     * @param user          用户
     * @param loginTenantId 登录选择的租户ID（多租户关闭时为null）
     */
    private void initSessionTenant(String token, User user, Long loginTenantId) {
        if (!TenantConstants.USER_TYPE_SUPER_ADMIN.equals(user.getUserType())) {
            return;
        }
        Long sessionTenantId = loginTenantId != null
                ? loginTenantId
                : (user.getTenantId() != null ? user.getTenantId() : tenantSupport.getDefaultTenantId());
        if (sessionTenantId == null) {
            return;
        }
        // 会话租户上下文与登录态同寿命，避免 token 过期后 Redis 键无限累积
        redisTemplate.opsForValue().set(TenantConstants.TENANT_CONTEXT_KEY_PREFIX + token, String.valueOf(sessionTenantId),
                authProperties.getTokenExpireTime(), TimeUnit.MINUTES);
    }

    /**
     * 使用临时token验证并修改密码
     *
     * @param tempToken 临时token
     * @param newPassword 新密码
     * @return 修改密码结果
     */
    @Override
    public Boolean changePasswordWithTempToken(String tempToken, String newPassword) {
        String tempTokenKey = "temp_token:" + tempToken;
        String userJson = redisTemplate.opsForValue().get(tempTokenKey);
        
        if (userJson == null) {
            throw new ArtException("临时令牌无效或已过期！");
        }
        
        // 从Redis中删除临时token（一次性使用）
        redisTemplate.delete(tempTokenKey);
        
        // 解析用户信息（仅用于定位用户，避免用登录时的旧快照回写覆盖最新状态）
        User snapshot = JSON.parseObject(userJson, User.class);
        if (snapshot == null || snapshot.getId() == null) {
            throw new ArtException("临时令牌无效或已过期！");
        }
        
        // 验证新密码是否为空
        if (StringUtils.isBlank(newPassword)) {
            throw new ArtException("新密码不能为空！");
        }
        
        // 以数据库当前状态为准：用户可能已被禁用/删除，或租户归属已调整
        User currentUser = tenantSupport.systemScope(() -> userService.getById(snapshot.getId()));
        if (currentUser == null) {
            throw new ArtException("用户不存在，请联系管理员！");
        }
        if (currentUser.getEnableFlag() != null && currentUser.getEnableFlag() == 0) {
            throw new ArtException("该用户已被禁用，请联系管理员！");
        }
        
        // 仅更新密码与首次登录标志，不覆盖其它字段
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User update = new User();
        update.setId(currentUser.getId());
        update.setPassword(encoder.encode(newPassword));
        update.setFirstLoginFlag(0); // 设置为非首次登录
        
        // 更新用户信息
        return tenantSupport.systemScope(() -> userService.updateById(update));
    }

    /**
     * 注销登出用户
     *
     * @return 注销登出结果
     */
    @Override
    public Boolean logout() {
        String token = StpUtil.getTokenInfo().getTokenValue();
        if (StringUtils.isBlank(token)) {
            return true;
        }
        StpUtil.logout();
        redisTemplate.delete("access_token:" + token);
        redisTemplate.delete(TenantConstants.TENANT_CONTEXT_KEY_PREFIX + token);
        return true;
    }

    /**
     * 用户重置密码
     *
     * @param userResetPasswordVO 用户重置密码参数
     * @return 重置密码结果
     */
    @Override
    public Boolean userResetPassword(UserResetPasswordVO userResetPasswordVO) {
        String oldPassword = userResetPasswordVO.getOldPassword();
        String newPassword = userResetPasswordVO.getNewPassword();
        if (StringUtil.isAnyBlank(oldPassword, newPassword, userResetPasswordVO.getConfirmPassword())) {
            throw new ArtException("表单填写不完整，请检查！");
        }
        if (!newPassword.equals(userResetPasswordVO.getConfirmPassword())) {
            throw new ArtException("两次输入新密码不一致，请检查！");
        }
        // 获取当前登录用户ID
        Long userId = SecurityUtil.getUserId();
        User user = this.userService.getById(userId);
        String currentPassword = user.getPassword();
        // 校验密码是否输入正确
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(oldPassword, currentPassword)) {
            throw new ArtException("原密码错误，请重新输入！");
        }
        // 重置密码
        String encodeNewPassword = encoder.encode(newPassword);
        user.setPassword(encodeNewPassword);
        return this.userService.updateById(user);
    }
}

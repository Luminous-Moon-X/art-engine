package com.art.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.alibaba.fastjson2.JSON;
import com.art.config.AuthConfiguration;
import com.art.domain.User;
import com.art.domain.vo.LoginResultVO;
import com.art.domain.vo.LoginVO;
import com.art.domain.vo.UserResetPasswordVO;
import com.art.exception.ArtException;
import com.art.event.LoginLogEvent;
import com.art.service.AuthService;
import com.art.service.UserService;
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
    private final AuthConfiguration authConfiguration;
    /**
     * 事件发布器
     */
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 构造器注入
     *
     * @param userService       用户表Service层逻辑
     * @param redisTemplate     Redis操作对象
     * @param authConfiguration 权限配置
     * @param eventPublisher    事件发布器
     */
    public AuthServiceImpl(UserService userService, RedisTemplate<String, String> redisTemplate,
                           AuthConfiguration authConfiguration, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.authConfiguration = authConfiguration;
        this.eventPublisher = eventPublisher;
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
        // 根据用户名查询用户信息
        List<User> userList = userService.list(QueryWrapper.create().eq(User::getUserName, username));
        if (CollectionUtil.isEmpty(userList)) {
            throw new ArtException("用户名或密码错误！");
        }

        User user = userList.getFirst();
        if (!user.getEnableFlag()) {
            throw new ArtException("该用户已被禁用，请联系管理员！");
        }
        // 对比密码
        String realPassword = user.getPassword();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, realPassword)) {
            throw new ArtException("用户名或密码错误！");
        }
        
        // 检查是否首次登录
        boolean isFirstLogin = user.getFirstLoginFlag() != null && user.getFirstLoginFlag();
        
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
                    authConfiguration.getTokenExpireTime(), TimeUnit.MINUTES);
            loginResultVO.setToken(token);
        }

        // 发布登录日志事件
        eventPublisher.publishEvent(new LoginLogEvent(user.getUserName(), user.getNickName(), request));

        return loginResultVO;
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
        
        // 解析用户信息
        User user = JSON.parseObject(userJson, User.class);
        
        // 验证新密码是否为空
        if (StringUtils.isBlank(newPassword)) {
            throw new ArtException("新密码不能为空！");
        }
        
        // 更新用户密码和首次登录标志
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(newPassword);
        user.setPassword(encodedPassword);
        user.setFirstLoginFlag(false); // 设置为非首次登录
        
        // 更新用户信息
        return userService.updateById(user);
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

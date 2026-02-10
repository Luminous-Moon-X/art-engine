package com.art.controller;

import com.art.common.HttpResult;
import com.art.domain.vo.LoginResultVO;
import com.art.domain.vo.LoginVO;
import com.art.domain.vo.UserResetPasswordVO;
import com.art.service.AuthService;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    /**
     * 认证服务接口
     */
    private final AuthService authService;

    /**
     * 用户表Service层逻辑
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录接口
     *
     * @return 登录结果
     */
    @PostMapping("/login")
    public HttpResult<LoginResultVO> login(@RequestBody LoginVO loginVO) {
        return HttpResult.success(this.authService.login(loginVO));
    }

    /**
     * 注销登出用户
     *
     * @return 注销登出结果
     */
    @PostMapping("/logout")
    public HttpResult<Boolean> logout() {
        return HttpResult.success(this.authService.logout());
    }

    /**
     * 用户重置密码
     *
     * @param userResetPasswordVO 用户重置密码参数
     * @return 重置密码结果
     */
    @PutMapping("/userResetPassword")
    public HttpResult<Boolean> userResetPassword(@RequestBody UserResetPasswordVO userResetPasswordVO) {
        return HttpResult.success(this.authService.userResetPassword(userResetPasswordVO));
    }
}

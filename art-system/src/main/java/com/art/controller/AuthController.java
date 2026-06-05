package com.art.controller;

import com.art.common.HttpResult;
import com.art.domain.vo.ForceChangePasswordVO;
import com.art.domain.vo.LoginResultVO;
import com.art.domain.vo.LoginVO;
import com.art.domain.vo.UserResetPasswordVO;
import com.art.exception.ArtException;
import com.art.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;

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
    public HttpResult<LoginResultVO> login(@RequestBody LoginVO loginVO, HttpServletRequest request) {
        return HttpResult.success(this.authService.login(loginVO, request));
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

    /**
     * 使用临时token修改密码（用于首次登录强制修改密码）
     *
     * @param forceChangePasswordVO 强制修改密码参数
     * @return 修改密码结果
     */
    @PutMapping("/changePasswordWithTempToken")
    public HttpResult<Boolean> changePasswordWithTempToken(@RequestBody ForceChangePasswordVO forceChangePasswordVO) {
        // 验证新密码和确认密码是否一致
        if (!forceChangePasswordVO.getNewPassword().equals(forceChangePasswordVO.getConfirmPassword())) {
            throw new ArtException("新密码与确认密码不一致！");
        }
        
        return HttpResult.success(this.authService.changePasswordWithTempToken(
                forceChangePasswordVO.getTempToken(), 
                forceChangePasswordVO.getNewPassword()));
    }
}

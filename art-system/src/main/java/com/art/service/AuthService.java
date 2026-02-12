package com.art.service;

import com.art.domain.vo.LoginResultVO;
import com.art.domain.vo.LoginVO;
import com.art.domain.vo.UserResetPasswordVO;

/**
 * 认证服务接口
 *
 * @author Luminous.X
 * @since 1.1.2
 */
public interface AuthService {
    /**
     * 登录接口
     *
     * @param loginVO 登录参数
     * @return 登录结果
     */
    LoginResultVO login(LoginVO loginVO);

    /**
     * 注销登出用户
     *
     * @return 注销登出结果
     */
    Boolean logout();

    /**
     * 用户重置密码
     *
     * @param userResetPasswordVO 用户重置密码参数
     * @return 重置密码结果
     */
    Boolean userResetPassword(UserResetPasswordVO userResetPasswordVO);

    /**
     * 使用临时token修改密码（用于首次登录强制修改密码）
     *
     * @param tempToken 临时token
     * @param newPassword 新密码
     * @return 修改密码结果
     */
    Boolean changePasswordWithTempToken(String tempToken, String newPassword);
}

package com.art.controller;

import com.art.art.common.HttpResult;
import com.art.domain.vo.UserInfoVO;
import com.art.service.SysUserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息接口
 *
 * @author Luminous.X
 * @since 2025年12月15日
 */
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    /**
     * 用户信息服务
     */
    private final SysUserService sysUserService;

    /**
     * 登录获取用户基本信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public HttpResult<UserInfoVO> info() {
        return HttpResult.success(sysUserService.info());
    }
}

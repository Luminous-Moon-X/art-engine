package com.art.controller;

import com.art.art.common.HttpResult;
import com.art.art.config.AuthConfiguration;
import com.art.art.exception.ArtException;
import com.art.domain.SysUser;
import com.art.domain.vo.LoginResultVO;
import com.art.domain.vo.LoginVO;
import com.art.domain.vo.LogoutVO;
import com.art.service.SysUserService;
import com.alibaba.fastjson2.JSON;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.util.CollectionUtil;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
public class AuthController {
    /**
     * 用户表Service层逻辑
     */
    private final SysUserService sysUserService;
    /**
     * Redis操作对象
     */
    private final RedisTemplate<String, String> redisTemplate;
    /**
     * 权限配置
     */
    private final AuthConfiguration authConfiguration;

    /**
     * 构造器注入
     *
     * @param sysUserService    用户表Service层逻辑
     * @param redisTemplate     Redis操作对象
     * @param authConfiguration 权限配置
     */
    public AuthController(SysUserService sysUserService, RedisTemplate<String, String> redisTemplate,
            AuthConfiguration authConfiguration) {
        this.sysUserService = sysUserService;
        this.redisTemplate = redisTemplate;
        this.authConfiguration = authConfiguration;
    }

    /**
     * 登录接口
     *
     * @return 登录结果
     */
    @PostMapping("/login")
    public HttpResult<LoginResultVO> login(@RequestBody LoginVO loginVO) {
        // 获取用户登录的账号和密码
        String username = loginVO.getUsername();
        String password = loginVO.getPassword();
        if (StringUtils.isAnyBlank(username, password)) {
            throw new ArtException("用户名或密码不能为空！");
        }
        // 根据用户名查询用户信息
        List<SysUser> userList = sysUserService.list(QueryWrapper.create().eq(SysUser::getUserName, username));
        if (CollectionUtil.isEmpty(userList)) {
            throw new ArtException("用户不存在！");
        }
        // 对比密码
        String realPassword = userList.getFirst().getPassword();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(password, realPassword)) {
            throw new ArtException("密码错误！");
        }
        StpUtil.login(userList.getFirst().getId(),
                new SaLoginParameter().setExtra("name", userList.getFirst().getUserName()));
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        String token = tokenInfo.getTokenValue();
        // 将token存储到Redis
        redisTemplate.opsForValue().set("access_token:" + token, JSON.toJSONString(userList.getFirst()),
                authConfiguration.getTokenExpireTime(), TimeUnit.MINUTES);
        return HttpResult.success(new LoginResultVO(token));
    }

    /**
     * 注销登出用户
     *
     * @param logoutVO 注销登出VO
     * @return 注销登出结果
     */
    @PostMapping("/logout")
    public HttpResult<Boolean> logout(@RequestBody LogoutVO logoutVO) {
        String token = StpUtil.getTokenInfo().getTokenValue();
        if (StringUtils.isBlank(token)) {
            return HttpResult.success(true);
        }
        redisTemplate.delete("access_token:" + token);
        return HttpResult.success(true);
    }
}

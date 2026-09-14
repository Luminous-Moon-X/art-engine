package com.art.service.impl;

import com.art.domain.SysUser;
import com.art.domain.User;
import com.art.domain.vo.UserInfoVO;
import com.art.exception.ArtException;
import com.art.mapper.SysUserMapper;
import com.art.service.SysUserService;
import com.art.service.TenantService;
import com.art.utils.SecurityUtil;
import com.alibaba.fastjson2.JSON;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 用户表Service增强接口层实现
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    /**
     * Redis客户端
     */
    private final RedisTemplate<String, String> redisTemplate;
    /**
     * 租户服务
     */
    private final TenantService tenantService;

    /**
     * 登录用户信息
     *
     * @return 用户信息
     */
    @Override
    public UserInfoVO info() {
        UserInfoVO userInfoVO = new UserInfoVO();
        String token = SecurityUtil.getToken();
        String userInfoJsonStr = redisTemplate.opsForValue().get("access_token:" + token);
        User userInfo = JSON.parseObject(userInfoJsonStr, User.class);
        if (userInfo == null) {
            throw new ArtException("获取用户信息失败，请联系管理员！");
        }
        userInfoVO.setUserId(userInfo.getId());
        userInfoVO.setUserName(userInfo.getNickName());
        userInfoVO.setEmail(userInfo.getUserEmail());
        userInfoVO.setUserType(userInfo.getUserType());
        // 当前生效租户（超级管理员切换租户后为切换后的租户）
        Long tenantId = SecurityUtil.getTenantId();
        userInfoVO.setTenantId(tenantId);
        userInfoVO.setTenantName(tenantService.getTenantName(tenantId));
        return userInfoVO;
    }
}
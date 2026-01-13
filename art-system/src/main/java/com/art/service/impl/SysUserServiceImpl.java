package com.art.service.impl;

import com.art.exception.ArtException;
import com.art.kits.SecurityUtil;
import com.art.domain.SysUser;
import com.art.domain.vo.UserInfoVO;
import com.art.mapper.SysUserMapper;
import com.art.service.SysUserService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 用户表Service增强接口层实现
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Service
@AllArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    /**
     * Redis客户端
     */
    private final RedisTemplate<String, String> redisTemplate;

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
        JSONObject userInfo = JSON.parseObject(userInfoJsonStr);
        if (userInfo == null) {
            throw new ArtException("获取用户信息失败，请联系管理员！");
        }
        userInfoVO.setUserId(userInfo.getLong("id"));
        userInfoVO.setUserName(userInfo.getString("userAllName"));
        userInfoVO.setEmail("690278565@QQ.com");
        userInfoVO.setRoles(new String[]{"R_SUPER"});
        return userInfoVO;
    }
}

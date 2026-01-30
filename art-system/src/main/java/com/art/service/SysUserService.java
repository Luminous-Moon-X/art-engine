package com.art.service;

import com.art.domain.SysUser;
import com.art.domain.vo.UserInfoVO;
import com.mybatisflex.core.service.IService;

/**
 * 用户表Service增强接口层
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public interface SysUserService extends IService<SysUser> {
    /**
     * 登录用户信息
     *
     * @return 用户信息
     */
    UserInfoVO info();
}

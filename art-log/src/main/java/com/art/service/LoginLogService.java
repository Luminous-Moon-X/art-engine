package com.art.service;

import com.art.domain.LoginLog;
import com.art.domain.vo.LoginLogVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 登录日志服务接口
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public interface LoginLogService extends IService<LoginLog> {
    /**
     * 分页查询登录日志
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    Page<LoginLog> queryPage(Page<LoginLog> page, LoginLogVO vo);
}

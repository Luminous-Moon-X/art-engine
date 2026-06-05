package com.art.service.impl;

import com.art.domain.LoginLog;
import com.art.domain.vo.LoginLogVO;
import com.art.mapper.LoginLogMapper;
import com.art.service.LoginLogService;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 登录日志服务实现类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogService {

    /**
     * 分页查询登录日志
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @Override
    public Page<LoginLog> queryPage(Page<LoginLog> page, LoginLogVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        // 时间范围查询
        if (vo.getLoginTimeStart() != null) {
            wrapper.ge(LoginLog::getLoginTime, vo.getLoginTimeStart());
        }
        if (vo.getLoginTimeEnd() != null) {
            wrapper.le(LoginLog::getLoginTime, vo.getLoginTimeEnd());
        }
        wrapper.orderBy(LoginLog::getLoginTime, false);
        return this.page(page, wrapper);
    }
}

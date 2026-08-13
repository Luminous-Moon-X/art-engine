package com.art.service.impl;

import com.art.context.IgnoreSqlLogContextHolder;
import com.art.domain.ApiLog;
import com.art.domain.vo.ApiLogVO;
import com.art.mapper.ApiLogMapper;
import com.art.service.ApiLogService;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 接口日志服务实现类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
public class ApiLogServiceImpl extends ServiceImpl<ApiLogMapper, ApiLog> implements ApiLogService {

    /**
     * 分页查询接口日志
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @Override
    public Page<ApiLog> queryPage(Page<ApiLog> page, ApiLogVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        if (vo.getRequestTimeStart() != null) {
            wrapper.ge(ApiLog::getRequestTime, vo.getRequestTimeStart());
        }
        if (vo.getRequestTimeEnd() != null) {
            wrapper.le(ApiLog::getRequestTime, vo.getRequestTimeEnd());
        }
        wrapper.orderBy(ApiLog::getRequestTime, false);
        return this.page(page, wrapper);
    }

    /**
     * 异步保存接口日志
     *
     * @param apiLog 接口日志实体
     */
    @Override
    @Async
    public void saveAsync(ApiLog apiLog) {
        try {
            IgnoreSqlLogContextHolder.enable();
            this.save(apiLog);
        } finally {
            IgnoreSqlLogContextHolder.disable();
        }

    }
}

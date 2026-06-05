package com.art.log.service;

import com.art.log.domain.ApiLog;
import com.art.log.domain.vo.ApiLogVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 接口日志服务接口
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public interface ApiLogService extends IService<ApiLog> {
    /**
     * 分页查询接口日志
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    Page<ApiLog> queryPage(Page<ApiLog> page, ApiLogVO vo);

    /**
     * 异步保存接口日志
     *
     * @param apiLog 接口日志实体
     */
    void saveAsync(ApiLog apiLog);
}

package com.art.log.service;

import com.art.log.domain.MenuLog;
import com.art.log.domain.vo.MenuLogVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

/**
 * 菜单日志服务接口
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public interface MenuLogService extends IService<MenuLog> {
    /**
     * 分页查询菜单日志
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    Page<MenuLog> queryPage(Page<MenuLog> page, MenuLogVO vo);

    /**
     * 记录菜单日志
     *
     * @param menuName 菜单名称
     * @param menuPath 菜单路径
     */
    void recordMenuLog(String menuName, String menuPath);
}

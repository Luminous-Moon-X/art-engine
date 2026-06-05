package com.art.service.impl;

import com.art.domain.MenuLog;
import com.art.domain.vo.MenuLogVO;
import com.art.mapper.MenuLogMapper;
import com.art.service.MenuLogService;
import com.art.utils.QueryHelper;
import com.art.utils.SecurityUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 菜单日志服务实现类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
public class MenuLogServiceImpl extends ServiceImpl<MenuLogMapper, MenuLog> implements MenuLogService {

    /**
     * 分页查询菜单日志
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @Override
    public Page<MenuLog> queryPage(Page<MenuLog> page, MenuLogVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        if (vo.getClickTimeStart() != null) {
            wrapper.ge(MenuLog::getClickTime, vo.getClickTimeStart());
        }
        if (vo.getClickTimeEnd() != null) {
            wrapper.le(MenuLog::getClickTime, vo.getClickTimeEnd());
        }
        wrapper.orderBy(MenuLog::getClickTime, false);
        return this.page(page, wrapper);
    }

    /**
     * 记录菜单日志
     *
     * @param menuName 菜单名称
     * @param menuPath 菜单路径
     */
    @Override
    public void recordMenuLog(String menuName, String menuPath) {
        MenuLog menuLog = new MenuLog();
        menuLog.setUserId(SecurityUtil.getUserId());
        menuLog.setUserName(SecurityUtil.getUserName());
        menuLog.setNickName(SecurityUtil.getUserAllName());
        menuLog.setMenuName(menuName);
        menuLog.setMenuPath(menuPath);
        menuLog.setClickTime(LocalDateTime.now());
        this.save(menuLog);
    }
}

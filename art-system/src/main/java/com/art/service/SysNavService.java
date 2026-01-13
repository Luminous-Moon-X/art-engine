package com.art.service;

import com.art.common.CommonDataVO;
import com.art.common.CommonSearchVO;
import com.art.domain.SysNav;
import com.art.domain.vo.NavTreeVO;
import com.art.domain.vo.SystemNavVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;


import java.util.List;

/**
 * 导航表Service接口类
 *
 * @author Luminous X.
 * @since 0.0.1
 */
public interface SysNavService extends IService<SysNav> {
    /**
     * 分页查询导航
     *
     * @param commonSearchVO 通用查询对象
     * @return 分页数据对象
     */
    Page<SystemNavVO> queryPage(CommonSearchVO commonSearchVO);

    /**
     * 获取当前用户导航信息
     *
     * @return 当前用户导航信息
     */
    List<NavTreeVO> navTree();

    /**
     * 根据父级ID查询子导航信息
     *
     * @param parentId 父级ID
     * @return 子导航集合
     */
    List<SystemNavVO> queryChildren(String parentId);

    /**
     * 新增导航
     *
     * @param commonDataVO 通用数据VO类
     * @return 新增结果
     */
    Boolean insertData(CommonDataVO commonDataVO);

    /**
     * 修改导航
     *
     * @param commonDataVO 通用数据VO类
     * @return 修改结果
     */
    Boolean updateData(CommonDataVO commonDataVO);
}

package com.art.service;

import com.art.domain.Menu;
import com.art.domain.vo.MenuVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 菜单服务接口类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
public interface MenuService extends IService<Menu> {
    /**
     * 根据ID查询菜单信息
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    Menu selectById(Long id);

    /**
     * 分页查询菜单信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 菜单信息
     */
    Page<MenuVO> queryPage(Page<MenuVO> page, MenuVO vo);

    /**
     * 查询所有菜单信息
     *
     * @return 菜单信息
     */
    List<Menu> selectList();

    /**
     * 添加菜单信息
     *
     * @param vo 菜单信息
     * @return 添加结果
     */
    Boolean add(MenuVO vo);

    /**
     * 编辑菜单信息
     *
     * @param vo 菜单信息
     * @return 编辑结果
     */
    Boolean edit(MenuVO vo);

    /**
     * 删除菜单信息
     *
     * @param idList 菜单ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);
}

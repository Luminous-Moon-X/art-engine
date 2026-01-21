package com.art.service.impl;

import com.art.cache.MenuCache;
import com.art.domain.Menu;
import com.art.domain.vo.MenuTreeVO;
import com.art.domain.vo.MenuVO;
import com.art.exception.ArtException;
import com.art.kits.ConvertUtil;
import com.art.kits.QueryHelper;
import com.art.kits.StringUtil;
import com.art.mapper.MenuMapper;
import com.art.service.MenuService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 菜单服务实现类。
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    /**
     * 菜单缓存
     */
    private final MenuCache menuCache;

    /**
     * 构造函数
     *
     * @param menuCache     菜单缓存
     */
    public MenuServiceImpl(MenuCache menuCache) {
        this.menuCache = menuCache;
    }

    /**
     * 根据ID查询菜单信息
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    @Override
    public MenuVO selectById(Long id) {
        return this.getMapper().selectOneWithRelationsByIdAs(id, MenuVO.class);
    }

    /**
     * 分页查询菜单信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 菜单信息
     */
    @Override
    public Page<MenuVO> queryPage(Page<MenuVO> page, MenuVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        if (StringUtil.isAllBlank(vo.getMenuName(), vo.getRoutePath())) {
            wrapper.eq(Menu::getParentId, -1);
        }
        Page<MenuVO> menuPage = this.getMapper().paginateAs(page, wrapper, MenuVO.class);
        List<MenuVO> records = menuPage.getRecords();
        List<MenuVO> recordsWidthChildren = this.handleChildren(records);
        menuPage.setRecords(recordsWidthChildren);
        return menuPage;
    }

    /**
     * 处理子菜单
     *
     * @param records 菜单信息
     * @return 处理后的菜单信息
     */
    private List<MenuVO> handleChildren(List<MenuVO> records) {
        for (MenuVO menuVO : records) {
            QueryWrapper wrapper = QueryWrapper.create();
            wrapper.eq(Menu::getParentId, menuVO.getId());
            List<MenuVO> children = this.getMapper().selectListByQueryAs(wrapper, MenuVO.class);
            if (!children.isEmpty()) {
                menuVO.setChildren(children);
                this.handleChildren(children);
            }
        }
        return records;
    }

    /**
     * 查询所有菜单信息
     *
     * @return 菜单信息
     */
    @Override
    public List<Menu> selectList() {
        return this.list();
    }

    /**
     * 添加菜单信息
     *
     * @param vo 菜单信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(MenuVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Menu entity = ConvertUtil.convert(vo, Menu.class);
        boolean save = this.save(entity);
        if (save) {
            Thread.ofVirtual().start(menuCache::init);
        }
        return save;
    }

    /**
     * 编辑菜单信息
     *
     * @param vo 菜单信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(MenuVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Menu entity = ConvertUtil.convert(vo, Menu.class);
        boolean edit = this.updateById(entity);
        if (edit) {
            Thread.ofVirtual().start(menuCache::init);
        }
        return edit;
    }

    /**
     * 删除菜单信息
     *
     * @param idList 菜单ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        for (Long id : idList) {
            QueryWrapper wrapper = QueryWrapper.create();
            wrapper.eq(Menu::getParentId, id);
            long childCount = this.getMapper().selectCountByQuery(wrapper);
            if (childCount > 0) {
                throw new ArtException("该菜单有下级，无法删除！");
            }
        }
        return this.removeByIds(idList);
    }

    /**
     * 获取菜单树
     *
     * @return 菜单树
     */
    @Override
    public List<MenuTreeVO> menuTree() {
        return menuCache.get();
    }
}

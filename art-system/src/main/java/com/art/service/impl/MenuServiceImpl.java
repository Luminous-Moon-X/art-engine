package com.art.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.art.cache.MenuButtonCache;
import com.art.cache.MenuCache;
import com.art.common.TreeSelectVO;
import com.art.domain.Menu;
import com.art.domain.vo.MenuOperationPermissionVO;
import com.art.domain.vo.MenuTreeVO;
import com.art.domain.vo.MenuVO;
import com.art.exception.ArtException;
import com.art.service.MenuPermissionService;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.art.utils.StringUtil;
import com.art.mapper.MenuMapper;
import com.art.service.MenuService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 菜单服务实现类。
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Slf4j
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    /**
     * 菜单缓存
     */
    private final MenuCache menuCache;

    /**
     * 菜单按钮缓存
     */
    private final MenuButtonCache menuButtonCache;

    /**
     * 菜单权限服务
     */
    private final MenuPermissionService menuPermissionService;

    /**
     * 构造函数
     *
     * @param menuCache             菜单缓存
     * @param menuButtonCache       菜单按钮缓存
     * @param menuPermissionService 菜单权限服务
     */
    public MenuServiceImpl(MenuCache menuCache, MenuButtonCache menuButtonCache, MenuPermissionService menuPermissionService) {
        this.menuCache = menuCache;
        this.menuButtonCache = menuButtonCache;
        this.menuPermissionService = menuPermissionService;
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
        wrapper.orderBy(Menu::getOrderNum, true);
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
        List<MenuTreeVO> menuTreeVOList = menuCache.get();
        // 权限过滤
        List<String> menuPermission = this.menuPermissionService.getMenuPermission(null, null);
        return this.handleMenuPermission(menuTreeVOList, menuPermission);
    }

    /**
     * 处理菜单权限
     *
     * @param menuTreeVOList 菜单树
     * @param menuPermission 权限标识
     * @return 过滤权限后的菜单树
     */
    private List<MenuTreeVO> handleMenuPermission(List<MenuTreeVO> menuTreeVOList, List<String> menuPermission) {
        List<MenuTreeVO> permissionMenuList = new ArrayList<>();
        for (MenuTreeVO menuTreeVO : menuTreeVOList) {
            // 权限判断
            List<String> signList = new ArrayList<>();
            this.getAllSignByMenu(menuTreeVO, signList);
            if (!Collections.disjoint(signList, menuPermission)) {
                // 按钮
                List<MenuVO> menuButtonList = this.menuButtonCache.getByMenuIdAndPermission(Long.valueOf(menuTreeVO.getName()), menuPermission);
                if (!menuButtonList.isEmpty()) {
                    List<MenuOperationPermissionVO> menuAuth = menuButtonList.stream()
                            .map(menuVO -> new MenuOperationPermissionVO(menuVO.getMenuName(), menuVO.getPermissionSign()))
                            .toList();
                    menuTreeVO.getMeta().setAuthList(menuAuth);
                }
                // 递归 子菜单
                if (!CollectionUtil.isEmpty(menuTreeVO.getChildren())) {
                    List<MenuTreeVO> childMenuTree = handleMenuPermission(menuTreeVO.getChildren(), menuPermission);
                    menuTreeVO.setChildren(childMenuTree);
                }
                permissionMenuList.add(menuTreeVO);
            }
        }
        return permissionMenuList;
    }

    /**
     * 获取指定菜单下所有权限标识
     *
     * @param menuTree 菜单树节点对象
     */
    private void getAllSignByMenu(MenuTreeVO menuTree, List<String> signList) {
        signList.add(menuTree.getMeta().getPermissionSign());
        if (!CollectionUtil.isEmpty(menuTree.getChildren())) {
            for (MenuTreeVO menuTreeVO : menuTree.getChildren()) {
                getAllSignByMenu(menuTreeVO, signList);
            }
        }
    }

    /**
     * 获取所有菜单树
     *
     * @return 菜单树
     */
    @Override
    public List<TreeSelectVO> allMenuTree() {
        List<TreeSelectVO> resultList = new ArrayList<>();
        List<MenuTreeVO> menuTreeVOList = menuCache.get();
        this.buildAllTree(resultList, menuTreeVOList);
        return resultList;
    }

    /**
     * 构建所有菜单树
     *
     * @param resultList     结果列表
     * @param menuTreeVOList 菜单树VO列表
     */
    private void buildAllTree(List<TreeSelectVO> resultList, List<MenuTreeVO> menuTreeVOList) {
        for (MenuTreeVO menuTreeVO : menuTreeVOList) {
            TreeSelectVO treeSelectVO = new TreeSelectVO();
            treeSelectVO.setLabel(menuTreeVO.getMeta().getTitle());
            treeSelectVO.setValue(menuTreeVO.getMeta().getPermissionSign());
            if (!CollectionUtil.isEmpty(menuTreeVO.getChildren())) {
                List<TreeSelectVO> child = new ArrayList<>();
                buildAllTree(child, menuTreeVO.getChildren());
                treeSelectVO.setChildren(child);
            }
            resultList.add(treeSelectVO);
        }
    }
}

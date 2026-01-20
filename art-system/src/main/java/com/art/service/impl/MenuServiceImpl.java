package com.art.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.art.context.IgnoreSqlLogContextHolder;
import com.art.domain.Menu;
import com.art.domain.vo.MenuMetaVO;
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
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单服务实现类。
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Slf4j
@Service
@DependsOn("hikariLoader")
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    /**
     * Redis模板
     */
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 根菜单树
     */
    public MenuServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
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
        return this.save(entity);
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
        return this.updateById(entity);
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
        List<MenuTreeVO> menuTreeList;
        // 先查缓存
        String menuTreeJsonString = redisTemplate.opsForValue().get("menuTree");
        if (StringUtil.isNotBlank(menuTreeJsonString)) {
            menuTreeList = JSONArray.parseArray(menuTreeJsonString, MenuTreeVO.class);
        } else {
            // 缓存没有，查库并刷新缓存
            menuTreeList = this.initMenuTree();
        }
        return menuTreeList;
    }

    /**
     * 初始化菜单树
     *
     * @return 菜单树
     */
    @PostConstruct
    public List<MenuTreeVO> initMenuTree() {
        IgnoreSqlLogContextHolder.enable();
        QueryWrapper wrapper = QueryWrapper.create().eq(Menu::getParentId, -1);
        List<MenuVO> menuList = this.getMapper().selectListByQueryAs(wrapper, MenuVO.class);
        List<MenuTreeVO> menuTreeList = this.handleMenuTree(menuList);
        redisTemplate.opsForValue().set("menuTree", JSON.toJSONString(menuTreeList));
        IgnoreSqlLogContextHolder.disable();
        log.info("初始化菜单数据成功");
        return menuTreeList;
    }

    /**
     * 处理菜单树
     *
     * @param menuList 菜单信息
     * @return 处理后的菜单树
     */
    private List<MenuTreeVO> handleMenuTree(List<MenuVO> menuList) {
        List<MenuTreeVO> menuTreeList = new ArrayList<>();
        for (MenuVO menuVO : menuList) {
            MenuTreeVO menuTreeVO = new MenuTreeVO();
            menuTreeVO.setName(menuVO.getId().toString());
            menuTreeVO.setPath(menuVO.getRoutePath());
            menuTreeVO.setComponent(menuVO.getComponentPath());
            // 封装Meta
            MenuMetaVO meta = getMenuMetaVO(menuVO);
            menuTreeVO.setMeta(meta);
            // 判断是否有子菜单
            List<MenuVO> childMenuList = this.getMapper().selectListByQueryAs(QueryWrapper.create().eq(Menu::getParentId, menuVO.getId()), MenuVO.class);
            if (!childMenuList.isEmpty()) {
                menuTreeVO.setChildren(this.handleMenuTree(childMenuList));
            }
            menuTreeList.add(menuTreeVO);
        }
        return menuTreeList;
    }

    /**
     * 封装Meta对象
     *
     * @param menuVO 菜单信息
     * @return 菜单Meta
     */
    private static MenuMetaVO getMenuMetaVO(MenuVO menuVO) {
        MenuMetaVO meta = new MenuMetaVO();
        meta.setIcon(menuVO.getMenuIcon());
        meta.setActivePath(menuVO.getActivationPath());
        meta.setTitle(menuVO.getMenuName());
        meta.setKeepAlive(menuVO.getKeepAlive());
        meta.setFixedTab(menuVO.getFixedTab());
        meta.setShowBadge(menuVO.getShowBadge());
        meta.setIsHide(menuVO.getHideFlag());
        meta.setIsHideTab(menuVO.getHideTab());
        meta.setLink(menuVO.getExternalLink());
        meta.setIsIframe(menuVO.getIframeFlag());
        meta.setIsFullScreen(menuVO.getFullScreen());
        return meta;
    }
}

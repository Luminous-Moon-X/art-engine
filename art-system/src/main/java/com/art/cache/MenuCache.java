package com.art.cache;


import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.domain.Menu;
import com.art.domain.vo.MenuMetaVO;
import com.art.domain.vo.MenuTreeVO;
import com.art.domain.vo.MenuVO;
import com.art.mapper.MenuMapper;
import com.art.tenant.TenantSupport;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树数据二级缓存实现（前端路由展示业务缓存，归属 art-system）
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Component
public class MenuCache extends ArtCache<List<MenuTreeVO>> {

    /**
     * 菜单Mapper
     */
    private final MenuMapper menuMapper;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param properties    缓存配置
     * @param menuMapper    菜单Mapper
     * @param tenantSupport 多租户支持
     */
    public MenuCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, MenuMapper menuMapper,
                     TenantSupport tenantSupport) {
        super(redisTemplate, properties);
        this.menuMapper = menuMapper;
        this.tenantSupport = tenantSupport;
    }

    @Override
    public String cacheName() {
        return "菜单树数据";
    }

    @Override
    public String redisKey() {
        return "menuTree";
    }

    @Override
    protected List<MenuTreeVO> loadFromDb() {
        // 菜单为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            // 仅加载启用中的菜单，禁用（enableFlag=0）菜单不出现在导航树中
            QueryWrapper wrapper = QueryWrapper.create().eq(Menu::getParentId, -1).eq(Menu::getEnableFlag, 1);
            wrapper.orderBy(Menu::getOrderNum, true);
            List<MenuVO> menuList = menuMapper.selectListByQueryAs(wrapper, MenuVO.class);
            return this.handleMenuTree(menuList);
        });
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
            // 判断是否有子菜单（子菜单同样只加载启用中的菜单）
            QueryWrapper wrapper = QueryWrapper.create()
                    .eq(Menu::getParentId, menuVO.getId())
                    .eq(Menu::getMenuType, "menu")
                    .eq(Menu::getEnableFlag, 1)
                    .orderBy(Menu::getOrderNum, true);
            List<MenuVO> childMenuList = menuMapper.selectListByQueryAs(wrapper, MenuVO.class);
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
        meta.setPermissionSign(menuVO.getPermissionSign());
        return meta;
    }
}

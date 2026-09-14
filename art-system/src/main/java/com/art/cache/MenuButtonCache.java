package com.art.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.domain.Menu;
import com.art.domain.vo.MenuVO;
import com.art.mapper.MenuMapper;
import com.art.tenant.TenantSupport;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单按钮数据二级缓存实现（业务缓存，归属 art-system）
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class MenuButtonCache extends ArtCache<List<MenuVO>> {
    /**
     * 菜单按钮Mapper
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
     * @param menuMapper    菜单按钮Mapper
     * @param tenantSupport 多租户支持
     */
    public MenuButtonCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, MenuMapper menuMapper,
                           TenantSupport tenantSupport) {
        super(redisTemplate, properties);
        this.menuMapper = menuMapper;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    public String cacheName() {
        return "菜单按钮";
    }

    /**
     * 获取缓存Key
     *
     * @return 缓存Key
     */
    @Override
    public String redisKey() {
        return "menuButton";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<MenuVO> loadFromDb() {
        // 菜单按钮为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            // 仅加载启用中的按钮，禁用（enableFlag=0）按钮不参与功能权限
            QueryWrapper wrapper = QueryWrapper.create().eq(Menu::getMenuType, "button").eq(Menu::getEnableFlag, 1);
            return this.menuMapper.selectListByQueryAs(wrapper, MenuVO.class);
        });
    }

    /**
     * 根据菜单ID获取按钮
     *
     * @param menuId 菜单ID
     * @return 按钮
     */
    public List<MenuVO> getByMenuId(Long menuId) {
        List<MenuVO> allMenuButton = this.get();
        return allMenuButton.stream().filter(menuVO -> menuVO.getParentId().equals(menuId)).toList();
    }

    /**
     * 根据菜单ID和权限获取按钮
     *
     * @param menuId          菜单ID
     * @param permissionSigns 权限
     * @return 按钮
     */
    public List<MenuVO> getByMenuIdAndPermission(Long menuId, List<String> permissionSigns) {
        List<MenuVO> allMenuButton = this.get();
        return allMenuButton.stream()
                .filter(menuVO -> menuVO.getParentId().equals(menuId) && permissionSigns.contains(menuVO.getPermissionSign()))
                .toList();
    }
}

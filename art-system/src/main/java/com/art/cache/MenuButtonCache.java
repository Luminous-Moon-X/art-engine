package com.art.cache;

import com.art.ArtCache;
import com.art.domain.Menu;
import com.art.domain.vo.MenuVO;
import com.art.mapper.MenuMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单按钮数据二级缓存实现
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class MenuButtonCache extends ArtCache<String, List<MenuVO>> {
    /**
     * 菜单按钮Mapper
     */
    private final MenuMapper menuMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param menuMapper    菜单按钮Mapper
     */
    public MenuButtonCache(RedisTemplate<String, Object> redisTemplate, MenuMapper menuMapper) {
        super(redisTemplate);
        this.menuMapper = menuMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    protected String getCacheName() {
        return "菜单按钮";
    }

    /**
     * 获取RedisKey
     *
     * @return RedisKey
     */
    @Override
    protected String getRedisKey() {
        return "menuButton";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<MenuVO> getCacheData() {
        QueryWrapper wrapper = QueryWrapper.create().eq(Menu::getMenuType, "button");
        return this.menuMapper.selectListByQueryAs(wrapper, MenuVO.class);
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

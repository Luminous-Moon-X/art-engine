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
 * 菜单权限标识二级缓存实现
 *
 * @author Luminous.X
 * @since 1.1.1
 */
@Component
public class MenuAuthCache extends ArtCache<String, List<String>> {

    /**
     * 菜单服务
     */
    private final MenuMapper menuMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param menuMapper    菜单服务
     */
    public MenuAuthCache(RedisTemplate<String, Object> redisTemplate, MenuMapper menuMapper) {
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
        return "菜单权限标识";
    }

    /**
     * 获取RedisKey
     *
     * @return RedisKey
     */
    @Override
    protected String getRedisKey() {
        return "allMenuAuth";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<String> getCacheData() {
        return this.menuMapper.selectListByQueryAs(QueryWrapper.create().eq(Menu::getEnableFlag, true), MenuVO.class)
                .stream()
                .map(MenuVO::getPermissionSign)
                .toList();
    }
}

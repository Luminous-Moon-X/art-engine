package com.art.auth.cache;

import com.art.cache.support.ArtCache;
import com.art.cache.support.ArtCacheProperties;
import com.art.domain.Menu;
import com.art.domain.vo.MenuVO;
import com.art.mapper.MenuMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 菜单权限标识二级缓存实现（认证授权基础设施缓存，归属 art-core）
 *
 * @author Luminous.X
 * @since 1.1.1
 */
@Component
public class MenuAuthCache extends ArtCache<List<String>> {

    /**
     * 菜单Mapper
     */
    private final MenuMapper menuMapper;

    /**
     * 构造函数
     *
     * @param redisTemplate Redis客户端
     * @param properties    缓存配置
     * @param menuMapper    菜单Mapper
     */
    public MenuAuthCache(RedisTemplate<String, Object> redisTemplate, ArtCacheProperties properties, MenuMapper menuMapper) {
        super(redisTemplate, properties);
        this.menuMapper = menuMapper;
    }

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    @Override
    public String cacheName() {
        return "菜单权限标识";
    }

    /**
     * 获取缓存Key
     *
     * @return 缓存Key
     */
    @Override
    public String redisKey() {
        return "allMenuAuth";
    }

    /**
     * 获取缓存数据
     *
     * @return 缓存数据
     */
    @Override
    protected List<String> loadFromDb() {
        return this.menuMapper.selectListByQueryAs(QueryWrapper.create().eq(Menu::getEnableFlag, 1), MenuVO.class)
                .stream()
                .map(MenuVO::getPermissionSign)
                .toList();
    }
}

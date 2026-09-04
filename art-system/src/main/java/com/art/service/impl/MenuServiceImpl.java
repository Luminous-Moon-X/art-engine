package com.art.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.art.auth.cache.MenuAuthCache;
import com.art.cache.MenuButtonCache;
import com.art.cache.MenuCache;
import com.art.cache.support.CacheRefreshService;
import com.art.common.TreeSelectVO;
import com.art.domain.Menu;
import com.art.domain.vo.MenuOperationPermissionVO;
import com.art.domain.vo.MenuTreeVO;
import com.art.domain.vo.MenuVO;
import com.art.exception.ArtException;
import com.art.service.MenuPermissionService;
import com.art.service.TenantService;
import com.art.tenant.TenantSupport;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.art.utils.SecurityUtil;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * 菜单权限标识缓存
     */
    private final MenuAuthCache menuAuthCache;

    /**
     * 缓存刷新服务
     */
    private final CacheRefreshService cacheRefreshService;

    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 租户服务
     */
    private final TenantService tenantService;

    /**
     * 构造函数
     *
     * @param menuCache             菜单缓存
     * @param menuButtonCache       菜单按钮缓存
     * @param menuPermissionService 菜单权限服务
     * @param menuAuthCache         菜单权限标识缓存
     * @param cacheRefreshService   缓存刷新服务
     * @param tenantSupport         多租户支持
     * @param tenantService         租户服务
     */
    public MenuServiceImpl(MenuCache menuCache, MenuButtonCache menuButtonCache, MenuPermissionService menuPermissionService,
                           MenuAuthCache menuAuthCache, CacheRefreshService cacheRefreshService,
                           TenantSupport tenantSupport, TenantService tenantService) {
        this.menuCache = menuCache;
        this.menuButtonCache = menuButtonCache;
        this.menuPermissionService = menuPermissionService;
        this.menuAuthCache = menuAuthCache;
        this.cacheRefreshService = cacheRefreshService;
        this.tenantSupport = tenantSupport;
        this.tenantService = tenantService;
    }

    /**
     * 根据ID查询菜单信息
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    @Override
    public MenuVO selectById(Long id) {
        // 菜单为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.getMapper().selectOneWithRelationsByIdAs(id, MenuVO.class));
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
        // 菜单为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
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
        });
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
        // 菜单为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.list());
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
        // 菜单为系统级数据，不受租户过滤
        boolean save = tenantSupport.systemScope(() -> this.save(entity));
        if (save) {
            cacheRefreshService.refreshAfterCommit(menuCache);
            cacheRefreshService.refreshAfterCommit(menuButtonCache);
            cacheRefreshService.refreshAfterCommit(menuAuthCache);
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
        // 菜单为系统级数据，不受租户过滤
        boolean edit = tenantSupport.systemScope(() -> this.updateById(entity));
        if (edit) {
            cacheRefreshService.refreshAfterCommit(menuCache);
            cacheRefreshService.refreshAfterCommit(menuButtonCache);
            cacheRefreshService.refreshAfterCommit(menuAuthCache);
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
        // 菜单为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            for (Long id : idList) {
                QueryWrapper wrapper = QueryWrapper.create();
                wrapper.eq(Menu::getParentId, id);
                long childCount = this.getMapper().selectCountByQuery(wrapper);
                if (childCount > 0) {
                    throw new ArtException("该菜单有下级，无法删除！");
                }
            }
            boolean result = this.removeByIds(idList);
            if (result) {
                List<String> deletePermissionSigns = this.listByIds(idList).stream().map(Menu::getPermissionSign).toList();
                if (!deletePermissionSigns.isEmpty()) {
                    menuPermissionService.deletePermissionByMenu(deletePermissionSigns);
                    cacheRefreshService.refreshAfterCommit(menuCache);
                    cacheRefreshService.refreshAfterCommit(menuButtonCache);
                    cacheRefreshService.refreshAfterCommit(menuAuthCache);
                }
            }
            return result;
        });
    }

    /**
     * 获取菜单树（前端导航渲染）
     *
     * <p>多租户开启时：超级管理员返回全部菜单；租户管理员返回其租户套餐范围内的全部菜单；
     * 普通用户在套餐范围的基础上再按功能权限过滤。</p>
     *
     * @return 菜单树
     */
    @Override
    public List<MenuTreeVO> menuTree() {
        List<MenuTreeVO> menuTreeVOList = menuCache.get();
        String userType = SecurityUtil.getUserType();
        // 超级管理员：全部菜单
        if (tenantSupport.isSuperAdmin(userType)) {
            return menuTreeVOList;
        }
        // 非超管：剔除平台级管理菜单（菜单管理/字典管理/规则管理等），历史套餐误分配的平台菜单不再展示
        menuTreeVOList = this.filterPlatformMenus(menuTreeVOList);
        // 多租户未开启：保持历史行为（租户管理员=全局管理员；其他用户按权限过滤）
        if (!tenantSupport.isEnable()) {
            if (tenantSupport.isTenantAdmin(userType)) {
                return menuTreeVOList;
            }
            List<String> menuPermission = this.menuPermissionService.getMenuPermission(null, null);
            return this.handleMenuPermission(menuTreeVOList, menuPermission);
        }
        // 多租户开启：租户管理员 = 套餐内全部菜单；普通用户 = 套餐范围 ∩ 本人功能权限
        List<String> packageSigns = this.tenantService.getCurrentTenantPermissionSigns();
        if (packageSigns.isEmpty()) {
            // 套餐不限制范围
            if (tenantSupport.isTenantAdmin(userType)) {
                return menuTreeVOList;
            }
            List<String> menuPermission = this.menuPermissionService.getMenuPermission(null, null);
            return this.handleMenuPermission(menuTreeVOList, menuPermission);
        }
        List<String> scopeSigns = new ArrayList<>(packageSigns);
        if (!tenantSupport.isTenantAdmin(userType)) {
            List<String> userPermission = this.menuPermissionService.getMenuPermission(null, null);
            scopeSigns.retainAll(userPermission);
        }
        return this.handleMenuPermission(menuTreeVOList, scopeSigns);
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
        for (MenuTreeVO originMenuTreeVO : menuTreeVOList) {
            MenuTreeVO menuTreeVO = new MenuTreeVO(originMenuTreeVO.getName(), originMenuTreeVO.getPath(),
                    originMenuTreeVO.getComponent(), originMenuTreeVO.getMeta(), originMenuTreeVO.getChildren());
            // 首页跳过权限判断
            if ("/home".equals(menuTreeVO.getPath())) {
                permissionMenuList.add(menuTreeVO);
                continue;
            }
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
        } else {
            // 菜单按钮
            List<MenuVO> menuButtons = this.menuButtonCache.getByMenuId(Long.valueOf(menuTree.getName()));
            if (!menuButtons.isEmpty()) {
                signList.addAll(menuButtons.stream().map(MenuVO::getPermissionSign).toList());
            }
        }
    }

    /**
     * 获取所有菜单树（功能权限分配使用）
     *
     * <p>多租户开启时，非超级管理员只能看到其租户套餐范围内的菜单与按钮；</p>
     *
     * @return 菜单树
     */
    @Override
    public List<TreeSelectVO> allMenuTree() {
        List<TreeSelectVO> resultList = new ArrayList<>();
        // 平台级管理菜单对套餐/功能权限分配不可见（默认剔除，含历史数据）
        List<MenuTreeVO> menuTreeVOList = this.filterPlatformMenus(menuCache.get());
        Set<String> scopeSigns = this.resolvePackageScopeSigns();
        this.buildAllTree(resultList, menuTreeVOList, scopeSigns);
        return resultList;
    }

    /**
     * 从菜单树副本中剔除平台级管理菜单（含其子节点）<br/>
     * 仅复制结构，不修改缓存中的原始树；平台级菜单判定依据
     * {@link TenantSupport#isPlatformMenuSign(String)}（配置 art.tenant.platform-menu-sign-prefixes）
     *
     * @param menuTreeVOList 缓存中的菜单树
     * @return 剔除平台级菜单后的新菜单树
     */
    private List<MenuTreeVO> filterPlatformMenus(List<MenuTreeVO> menuTreeVOList) {
        if (CollectionUtil.isEmpty(menuTreeVOList)) {
            return new ArrayList<>();
        }
        List<MenuTreeVO> resultList = new ArrayList<>();
        for (MenuTreeVO originMenuTreeVO : menuTreeVOList) {
            if (tenantSupport.isPlatformMenuSign(originMenuTreeVO.getMeta().getPermissionSign())) {
                continue;
            }
            MenuTreeVO menuTreeVO = new MenuTreeVO(originMenuTreeVO.getName(), originMenuTreeVO.getPath(),
                    originMenuTreeVO.getComponent(), originMenuTreeVO.getMeta(), originMenuTreeVO.getChildren());
            if (!CollectionUtil.isEmpty(originMenuTreeVO.getChildren())) {
                menuTreeVO.setChildren(this.filterPlatformMenus(originMenuTreeVO.getChildren()));
            }
            resultList.add(menuTreeVO);
        }
        return resultList;
    }

    /**
     * 解析当前用户的套餐菜单范围（null=不限制；非空集合=仅限集合内菜单）
     *
     * @return 套餐菜单权限标识集合
     */
    private Set<String> resolvePackageScopeSigns() {
        String userType = SecurityUtil.getUserType();
        if (!tenantSupport.isEnable() || tenantSupport.isSuperAdmin(userType)) {
            return null;
        }
        List<String> packageSigns = this.tenantService.getCurrentTenantPermissionSigns();
        return packageSigns.isEmpty() ? null : new HashSet<>(packageSigns);
    }

    /**
     * 构建所有菜单树
     *
     * @param resultList     结果列表
     * @param menuTreeVOList 菜单树VO列表
     * @param scopeSigns     套餐菜单范围（null=不限制）
     */
    private void buildAllTree(List<TreeSelectVO> resultList, List<MenuTreeVO> menuTreeVOList, Set<String> scopeSigns) {
        for (MenuTreeVO menuTreeVO : menuTreeVOList) {
            TreeSelectVO treeSelectVO = new TreeSelectVO();
            treeSelectVO.setLabel(menuTreeVO.getMeta().getTitle());
            treeSelectVO.setValue(menuTreeVO.getMeta().getPermissionSign());
            List<TreeSelectVO> child = new ArrayList<>();
            if (!CollectionUtil.isEmpty(menuTreeVO.getChildren())) {
                buildAllTree(child, menuTreeVO.getChildren(), scopeSigns);
            } else {
                // 查询按钮
                List<TreeSelectVO> menuButtons = this.menuButtonCache.getByMenuId(Long.valueOf(menuTreeVO.getName()))
                        .stream()
                        .filter(menuVO -> scopeSigns == null || scopeSigns.contains(menuVO.getPermissionSign()))
                        .map(menuVO -> new TreeSelectVO(menuVO.getMenuName(), menuVO.getPermissionSign(), false, null))
                        .toList();
                if (!menuButtons.isEmpty()) {
                    child.addAll(menuButtons);
                }
            }
            // 菜单自身在范围内或存在范围内子节点时保留
            String menuSign = menuTreeVO.getMeta().getPermissionSign();
            if (scopeSigns == null || !child.isEmpty() || (menuSign != null && scopeSigns.contains(menuSign))) {
                treeSelectVO.setChildren(child);
                resultList.add(treeSelectVO);
            }
        }
    }
}
package com.art.service.impl;

import com.art.common.SelectVO;
import com.art.domain.Menu;
import com.art.domain.Tenant;
import com.art.domain.TenantPackage;
import com.art.domain.vo.TenantPackageVO;
import com.art.exception.ArtException;
import com.art.mapper.MenuMapper;
import com.art.mapper.TenantMapper;
import com.art.mapper.TenantPackageMapper;
import com.art.service.TenantPackageService;
import com.art.tenant.TenantSupport;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.art.utils.StringUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 租户套餐服务实现类（系统级数据，操作不受租户过滤）
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@Service
public class TenantPackageServiceImpl extends ServiceImpl<TenantPackageMapper, TenantPackage> implements TenantPackageService {
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;
    /**
     * 租户Mapper
     */
    private final TenantMapper tenantMapper;
    /**
     * 菜单Mapper（用于校验套餐勾选的菜单权限标识）
     */
    private final MenuMapper menuMapper;

    /**
     * 构造函数
     *
     * @param tenantSupport 多租户支持
     * @param tenantMapper  租户Mapper
     * @param menuMapper    菜单Mapper
     */
    public TenantPackageServiceImpl(TenantSupport tenantSupport, TenantMapper tenantMapper, MenuMapper menuMapper) {
        this.tenantSupport = tenantSupport;
        this.tenantMapper = tenantMapper;
        this.menuMapper = menuMapper;
    }

    /**
     * 分页查询租户套餐信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 租户套餐信息
     */
    @Override
    public Page<TenantPackageVO> queryPage(Page<TenantPackageVO> page, TenantPackageVO vo) {
        // 租户套餐为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
            wrapper.orderBy(TenantPackage::getCreateTime, false);
            Page<TenantPackage> entityPage = this.getMapper().paginate(
                    new Page<>(page.getPageNumber(), page.getPageSize()), wrapper);
            List<TenantPackageVO> records = entityPage.getRecords().stream()
                    .map(this::convertToVO)
                    .toList();
            return new Page<TenantPackageVO>(records, entityPage.getPageNumber(),
                    entityPage.getPageSize(), entityPage.getTotalRow());
        });
    }

    /**
     * 查询所有启用中的租户套餐（下拉列表）
     *
     * @return 租户套餐下拉列表
     */
    @Override
    public List<SelectVO> select() {
        // 租户套餐为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            List<TenantPackage> packageList = this.list(QueryWrapper.create().eq(TenantPackage::getEnableFlag, 1));
            return packageList.stream().map(tenantPackage -> {
                SelectVO selectVO = new SelectVO();
                selectVO.setLabel(tenantPackage.getPackageName());
                selectVO.setValue(tenantPackage.getId());
                return selectVO;
            }).toList();
        });
    }

    /**
     * 添加租户套餐信息
     *
     * @param vo 租户套餐信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(TenantPackageVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        if (StringUtil.isBlank(vo.getPackageName())) {
            throw new ArtException("套餐名称不能为空！");
        }
        validatePermissionSigns(vo.getPermissionSigns());
        TenantPackage entity = ConvertUtil.convert(vo, TenantPackage.class);
        entity.setPermissionSigns(tenantSupport.joinSigns(vo.getPermissionSigns()));
        // 租户套餐为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.save(entity));
    }

    /**
     * 编辑租户套餐信息
     *
     * @param vo 租户套餐信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(TenantPackageVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        if (vo.getId() == null) {
            throw new ArtException("套餐ID不能为空！");
        }
        validatePermissionSigns(vo.getPermissionSigns());
        TenantPackage entity = ConvertUtil.convert(vo, TenantPackage.class);
        entity.setPermissionSigns(tenantSupport.joinSigns(vo.getPermissionSigns()));
        // 租户套餐为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.updateById(entity));
    }

    /**
     * 删除租户套餐信息
     *
     * @param idList 租户套餐ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        // 租户套餐为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            for (Long packageId : idList) {
                long tenantCount = tenantMapper.selectCountByQuery(QueryWrapper.create().eq(Tenant::getPackageId, packageId));
                if (tenantCount > 0) {
                    throw new ArtException("该套餐已被租户使用，无法删除！");
                }
            }
            return this.removeByIds(idList);
        });
    }

    /**
     * 根据ID查询租户套餐（含权限标识）
     *
     * @param id 租户套餐ID
     * @return 租户套餐信息
     */
    @Override
    public TenantPackageVO selectById(Long id) {
        // 租户套餐为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            TenantPackage tenantPackage = this.getById(id);
            return tenantPackage == null ? null : this.convertToVO(tenantPackage);
        });
    }

    /**
     * 实体转VO
     *
     * @param tenantPackage 租户套餐实体
     * @return 租户套餐VO
     */
    private TenantPackageVO convertToVO(TenantPackage tenantPackage) {
        TenantPackageVO vo = ConvertUtil.convert(tenantPackage, TenantPackageVO.class);
        List<String> signs = tenantSupport.splitSigns(tenantPackage.getPermissionSigns());
        vo.setPermissionSigns(signs);
        vo.setMenuCount(new HashSet<>(signs).size());
        return vo;
    }

    /**
     * 校验勾选的菜单权限标识是否在系统中存在，且不得包含平台级管理菜单
     *
     * @param permissionSigns 权限标识集合
     */
    private void validatePermissionSigns(List<String> permissionSigns) {
        if (permissionSigns == null || permissionSigns.isEmpty()) {
            return;
        }
        // 菜单为系统级数据，校验不受租户过滤
        tenantSupport.systemScope(() -> {
            Set<String> signs = new HashSet<>(permissionSigns);
            for (String sign : signs) {
                // 平台级管理菜单（菜单管理/字典管理/规则管理等）不允许分配给租户套餐
                if (tenantSupport.isPlatformMenuSign(sign)) {
                    throw new ArtException("平台级管理菜单权限标识【" + sign + "】不允许分配给租户套餐！");
                }
                long count = menuMapper.selectCountByQuery(QueryWrapper.create().eq(Menu::getPermissionSign, sign));
                if (count == 0) {
                    throw new ArtException("菜单权限标识【" + sign + "】不存在，请刷新菜单后重试！");
                }
            }
            return null;
        });
    }
}
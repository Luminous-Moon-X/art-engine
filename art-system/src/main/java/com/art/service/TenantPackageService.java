package com.art.service;

import com.art.common.SelectVO;
import com.art.domain.vo.TenantPackageVO;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * 租户套餐服务
 *
 * @author Luminous.X
 * @since 2.0.0
 */
public interface TenantPackageService {

    /**
     * 分页查询租户套餐信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 租户套餐信息
     */
    Page<TenantPackageVO> queryPage(Page<TenantPackageVO> page, TenantPackageVO vo);

    /**
     * 查询所有启用中的租户套餐（下拉列表）
     *
     * @return 租户套餐下拉列表
     */
    List<SelectVO> select();

    /**
     * 添加租户套餐信息
     *
     * @param vo 租户套餐信息
     * @return 添加结果
     */
    Boolean add(TenantPackageVO vo);

    /**
     * 编辑租户套餐信息
     *
     * @param vo 租户套餐信息
     * @return 编辑结果
     */
    Boolean edit(TenantPackageVO vo);

    /**
     * 删除租户套餐信息
     *
     * @param idList 租户套餐ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 根据ID查询租户套餐信息
     *
     * @param id 租户套餐ID
     * @return 租户套餐信息
     */
    TenantPackageVO selectById(Long id);
}
package com.art.service;

import com.art.domain.Tenant;
import com.art.domain.vo.TenantVO;
import com.art.tenant.TenantPermissionProvider;
import com.mybatisflex.core.paginate.Page;

import java.util.List;

/**
 * 租户服务
 *
 * @author Luminous.X
 * @since 2.0.0
 */
public interface TenantService extends TenantPermissionProvider {

    /**
     * 按ID查询启用中的租户
     *
     * @param tenantId 租户ID
     * @return 租户（不存在或已禁用返回null）
     */
    Tenant selectEnabledById(Long tenantId);

    /**
     * 分页查询租户信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 租户信息
     */
    Page<TenantVO> queryPage(Page<TenantVO> page, TenantVO vo);

    /**
     * 添加租户（同时创建该租户的管理员账号，密码使用规则表默认密码）
     *
     * @param vo 租户信息
     * @return 添加结果
     */
    Boolean add(TenantVO vo);

    /**
     * 编辑租户信息
     *
     * @param vo 租户信息
     * @return 编辑结果
     */
    Boolean edit(TenantVO vo);

    /**
     * 删除租户信息
     *
     * @param idList 租户ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 查询启用中的租户列表（登录页租户下拉框、超级管理员切换租户使用）
     *
     * @return 租户列表
     */
    List<TenantVO> listEnabled();

    /**
     * 切换当前登录用户的生效租户（仅超级管理员）
     *
     * @param tenantId 目标租户ID
     * @return 切换结果
     */
    Boolean switchTenant(Long tenantId);

    /**
     * 根据租户ID获取租户名称
     *
     * @param tenantId 租户ID
     * @return 租户名称
     */
    String getTenantName(Long tenantId);
}
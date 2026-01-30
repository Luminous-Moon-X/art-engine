package com.art.service;

import com.art.domain.Tenant;
import com.art.domain.vo.TenantVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 租户服务接口类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public interface TenantService extends IService<Tenant> {
    /**
     * 根据ID查询租户信息
     *
     * @param id 租户ID
     * @return 租户信息
     */
    Tenant selectById(Long id);

    /**
     * 分页查询租户信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 租户信息
     */
    Page<Tenant> queryPage(Page<Tenant> page, TenantVO vo);

    /**
     * 查询所有租户信息
     *
     * @return 租户信息
     */
    List<Tenant> selectList();

    /**
     * 添加租户信息
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
     * 切换租户启用状态
     *
     * @param id 租户ID
     * @return 切换结果
     */
    Boolean toggleEnable(Long id);
}

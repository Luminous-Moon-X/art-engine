package com.art.service;

import com.art.common.SelectVO;
import com.art.domain.Role;
import com.art.domain.vo.RoleVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 角色服务接口类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
public interface RoleService extends IService<Role> {
    /**
     * 根据ID查询角色信息
     *
     * @param id 角色ID
     * @return 角色信息
     */
    Role selectById(Long id);

    /**
     * 分页查询角色信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 角色信息
     */
    Page<RoleVO> queryPage(Page<RoleVO> page, RoleVO vo);

    /**
     * 查询所有角色信息
     *
     * @return 角色信息
     */
    List<Role> selectList();

    /**
     * 添加角色信息
     *
     * @param vo 角色信息
     * @return 添加结果
     */
    Boolean add(RoleVO vo);

    /**
     * 编辑角色信息
     *
     * @param vo 角色信息
     * @return 编辑结果
     */
    Boolean edit(RoleVO vo);

    /**
     * 删除角色信息
     *
     * @param idList 角色ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 查询角色下拉信息
     *
     * @return 角色下拉信息
     */
    List<SelectVO> select();
}

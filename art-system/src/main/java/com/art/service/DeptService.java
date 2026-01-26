package com.art.service;

import com.art.domain.Dept;
import com.art.domain.vo.DeptTreeSelectVO;
import com.art.domain.vo.DeptVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 部门表服务层
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
public interface DeptService extends IService<Dept> {
    /**
     * 根据ID查询部门信息
     *
     * @param id 部门ID
     * @return 部门信息
     */
    Dept selectById(Long id);

    /**
     * 分页查询部门信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 部门信息
     */
    Page<DeptVO> queryPage(Page<DeptVO> page, DeptVO vo);

    /**
     * 查询所有部门信息
     *
     * @return 部门信息
     */
    List<DeptVO> selectList();

    /**
     * 添加部门信息
     *
     * @param vo 部门信息
     * @return 添加结果
     */
    Boolean add(DeptVO vo);

    /**
     * 编辑部门信息
     *
     * @param vo 部门信息
     * @return 编辑结果
     */
    Boolean edit(DeptVO vo);

    /**
     * 删除部门信息
     *
     * @param idList 部门ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 部门树形下拉列表
     *
     * @return 部门树形下拉列表
     */
    List<DeptTreeSelectVO> treeSelect();

    /**
     * 部门树形下拉列表(不包含顶级部门)
     *
     * @return 部门树形下拉列表
     */
    List<DeptTreeSelectVO> treeSelectNoTop();
}

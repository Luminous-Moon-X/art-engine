package com.art.service.impl;

import com.art.domain.Dept;
import com.art.domain.vo.DeptTreeSelectVO;
import com.art.domain.vo.DeptVO;
import com.art.exception.ArtException;
import com.art.mapper.DeptMapper;
import com.art.service.DeptService;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.art.utils.StringUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 部门表服务实现类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, Dept> implements DeptService {
    /**
     * 根据ID查询部门信息
     *
     * @param id 部门ID
     * @return 部门信息
     */
    @Override
    public Dept selectById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页查询部门信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 部门信息
     */
    @Override
    public Page<DeptVO> queryPage(Page<DeptVO> page, DeptVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        if (StringUtil.isAllBlank(vo.getDeptName(), vo.getChargePerson())) {
            wrapper.eq(Dept::getParentId, -1);
        } else {
            return this.getMapper().paginateAs(page, wrapper, DeptVO.class);
        }
        Page<DeptVO> pageData = this.getMapper().paginateAs(page, wrapper, DeptVO.class);
        List<DeptVO> deptList = pageData.getRecords();
        List<DeptVO> deptListWithChild = this.handleChildren(deptList);
        pageData.setRecords(deptListWithChild);
        return pageData;
    }

    /**
     * 处理下级部门数据
     *
     * @param deptList 部门列表
     * @return 处理后的部门列表
     */
    private List<DeptVO> handleChildren(List<DeptVO> deptList) {
        for (DeptVO deptVO : deptList) {
            Long deptId = deptVO.getId();
            List<DeptVO> childDeptList = this.getMapper()
                    .selectListByQueryAs(QueryWrapper.create().eq(Dept::getParentId, deptId), DeptVO.class);
            if (!childDeptList.isEmpty()) {
                deptVO.setChildren(childDeptList);
                this.handleChildren(childDeptList);
            }
        }
        return deptList;
    }

    /**
     * 查询所有部门信息
     *
     * @return 部门信息
     */
    @Override
    public List<DeptVO> selectList() {
        List<DeptVO> deptList = this.listAs(QueryWrapper.create().eq(Dept::getParentId, -1), DeptVO.class);
        DeptVO topDept = new DeptVO();
        topDept.setDeptName("顶级部门");
        topDept.setId(-1L);
        topDept.setChildren(deptList);
        return this.handleChildren(Collections.singletonList(topDept));
    }

    /**
     * 添加部门信息
     *
     * @param vo 部门信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(DeptVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Dept entity = ConvertUtil.convert(vo, Dept.class);
        if (entity.getParentId() == null) {
            entity.setParentId(-1L);
        }
        return this.save(entity);
    }

    /**
     * 编辑部门信息
     *
     * @param vo 部门信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(DeptVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Dept entity = ConvertUtil.convert(vo, Dept.class);
        return this.updateById(entity);
    }

    /**
     * 删除部门信息
     *
     * @param idList 部门ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        long count = this.count(QueryWrapper.create().in(Dept::getParentId, idList));
        if (count > 0) {
            throw new ArtException("要删除的部门有下级部门，请检查！");
        }
        return this.removeByIds(idList);
    }

    /**
     * 部门树形下拉列表
     *
     * @return 部门树形下拉列表
     */
    @Override
    public List<DeptTreeSelectVO> treeSelect() {
        List<DeptTreeSelectVO> deptWidthChildList = this.treeSelectNoTop();
        DeptTreeSelectVO topTreeSelect = new DeptTreeSelectVO();
        topTreeSelect.setId(-1L);
        topTreeSelect.setDeptName("顶级节点");
        topTreeSelect.setChildren(deptWidthChildList);
        return Collections.singletonList(topTreeSelect);
    }

    /**
     * 部门树形下拉列表
     *
     * @return 部门树形下拉列表
     */
    @Override
    public List<DeptTreeSelectVO> treeSelectNoTop() {
        List<DeptTreeSelectVO> deptList = this.listAs(QueryWrapper.create().eq(Dept::getParentId, -1), DeptTreeSelectVO.class);
        return this.handleTreeChildren(deptList);
    }

    /**
     * 处理下级部门数据
     *
     * @param deptList 部门列表
     * @return 处理后的部门列表
     */
    private List<DeptTreeSelectVO> handleTreeChildren(List<DeptTreeSelectVO> deptList) {
        for (DeptTreeSelectVO deptTreeSelectVO : deptList) {
            Long deptId = deptTreeSelectVO.getId();
            List<DeptTreeSelectVO> childDeptList = this.getMapper()
                    .selectListByQueryAs(QueryWrapper.create().eq(Dept::getParentId, deptId), DeptTreeSelectVO.class);
            if (!childDeptList.isEmpty()) {
                deptTreeSelectVO.setChildren(childDeptList);
                this.handleTreeChildren(childDeptList);
            }
        }
        return deptList;
    }

}

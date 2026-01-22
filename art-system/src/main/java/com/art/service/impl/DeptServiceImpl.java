package com.art.service.impl;

import com.art.domain.Dept;
import com.art.domain.vo.DeptVO;
import com.art.exception.ArtException;
import com.art.mapper.DeptMapper;
import com.art.service.DeptService;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return this.getMapper().paginateAs(page, wrapper, DeptVO.class);
    }

    /**
     * 查询所有部门信息
     *
     * @return 部门信息
     */
    @Override
    public List<Dept> selectList() {
        return this.list();
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
        return this.removeByIds(idList);
    }

}

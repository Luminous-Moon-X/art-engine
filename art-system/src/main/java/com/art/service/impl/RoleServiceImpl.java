package com.art.service.impl;

import com.art.common.SelectVO;
import com.art.domain.Role;
import com.art.domain.vo.RoleVO;
import com.art.exception.ArtException;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.art.mapper.RoleMapper;
import com.art.service.RoleService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务实现类。
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    /**
     * 根据ID查询角色信息
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @Override
    public Role selectById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页查询角色信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 角色信息
     */
    @Override
    public Page<RoleVO> queryPage(Page<RoleVO> page, RoleVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        return this.getMapper().paginateAs(page, wrapper, RoleVO.class);
    }

    /**
     * 查询所有角色信息
     *
     * @return 角色信息
     */
    @Override
    public List<Role> selectList() {
        return this.list();
    }

    /**
     * 添加角色信息
     *
     * @param vo 角色信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(RoleVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Role entity = ConvertUtil.convert(vo, Role.class);
        return this.save(entity);
    }

    /**
     * 编辑角色信息
     *
     * @param vo 角色信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(RoleVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Role entity = ConvertUtil.convert(vo, Role.class);
        return this.updateById(entity);
    }

    /**
     * 删除角色信息
     *
     * @param idList 角色ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        return this.removeByIds(idList);
    }

    /**
     * 角色树形下拉列表
     *
     * @return 角色树形下拉列表
     */
    @Override
    public List<SelectVO> select() {
        List<Role> roleList = this.list(QueryWrapper.create().eq(Role::getEnableFlag, 1));
        return roleList.stream().map(role -> {
            SelectVO selectVO = new SelectVO();
            selectVO.setLabel(role.getRoleName());
            selectVO.setValue(role.getId());
            return selectVO;
        }).toList();
    }

}

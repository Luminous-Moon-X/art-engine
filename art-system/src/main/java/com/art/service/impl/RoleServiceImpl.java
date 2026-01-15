package com.art.service.impl;

import com.art.domain.Role;
import com.art.domain.vo.RoleVO;
import com.art.exception.ArtException;
import com.art.kits.ConvertUtil;
import com.art.kits.QueryHelper;
import com.art.mapper.RoleMapper;
import com.art.service.RoleService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现类。
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
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
    public Page<Role> queryPage(Page<Role> page, RoleVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        return this.getMapper().paginate(page, wrapper);
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
    public Boolean delete(List<Long> idList) {
        return this.removeByIds(idList);
    }

}

package com.art.service.impl;

import com.art.domain.Tenant;
import com.art.domain.vo.TenantVO;
import com.art.exception.ArtException;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.art.mapper.TenantMapper;
import com.art.service.TenantService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户服务实现类。
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {
    /**
     * 根据ID查询租户信息
     *
     * @param id 租户ID
     * @return 租户信息
     */
    @Override
    public Tenant selectById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页查询租户信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 租户信息
     */
    @Override
    public Page<Tenant> queryPage(Page<Tenant> page, TenantVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        return this.getMapper().paginate(page, wrapper);
    }

    /**
     * 查询所有租户信息
     *
     * @return 租户信息
     */
    @Override
    public List<Tenant> selectList() {
        return this.list();
    }

    /**
     * 添加租户信息
     *
     * @param vo 租户信息
     * @return 添加结果
     */
    @Override
    public Boolean add(TenantVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Tenant entity = ConvertUtil.convert(vo, Tenant.class);
        return this.save(entity);
    }

    /**
     * 编辑租户信息
     *
     * @param vo 租户信息
     * @return 编辑结果
     */
    @Override
    public Boolean edit(TenantVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Tenant entity = ConvertUtil.convert(vo, Tenant.class);
        return this.updateById(entity);
    }

    /**
     * 删除租户信息
     *
     * @param idList 租户ID列表
     * @return 删除结果
     */
    @Override
    public Boolean delete(List<Long> idList) {
        return this.removeByIds(idList);
    }

    /**
     * 切换租户启用状态
     *
     * @param id 租户ID
     * @return 切换结果
     */
    @Override
    public Boolean toggleEnable(Long id) {
        Tenant tenant = this.getById(id);
        if (tenant == null) {
            throw new ArtException("该租户不存在，请检查！");
        }
        tenant.setEnableFlag(!tenant.getEnableFlag()); // 切换启用状态
        return this.updateById(tenant);
    }
}

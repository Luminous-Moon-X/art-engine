package com.art.service.impl;

import com.art.domain.Dict;
import com.art.domain.DictValue;
import com.art.domain.vo.DictVO;
import com.art.exception.ArtException;
import com.art.mapper.DictMapper;
import com.art.service.DictService;
import com.art.service.DictValueService;
import com.art.tenant.TenantSupport;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典服务实现类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    /**
     * 字典值服务
     */
    private final DictValueService dictValueService;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param dictValueService 字典值服务
     * @param tenantSupport    多租户支持
     */
    public DictServiceImpl(DictValueService dictValueService, TenantSupport tenantSupport) {
        this.dictValueService = dictValueService;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 根据ID查询字典信息
     *
     * @param id 字典ID
     * @return 字典信息
     */
    @Override
    public Dict selectById(Long id) {
        // 字典为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.getById(id));
    }

    /**
     * 分页查询字典信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 字典信息
     */
    @Override
    public Page<DictVO> queryPage(Page<DictVO> page, DictVO vo) {
        // 字典为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
            return this.getMapper().paginateAs(page, wrapper, DictVO.class);
        });
    }

    /**
     * 查询所有字典信息
     *
     * @return 字典信息
     */
    @Override
    public List<Dict> selectList() {
        // 字典为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.list());
    }

    /**
     * 添加字典信息
     *
     * @param vo 字典信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(DictVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Dict entity = ConvertUtil.convert(vo, Dict.class);
        // 字典为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.save(entity));
    }

    /**
     * 编辑字典信息
     *
     * @param vo 字典信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(DictVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Dict entity = ConvertUtil.convert(vo, Dict.class);
        // 字典为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.updateById(entity));
    }

    /**
     * 删除字典信息
     *
     * @param idList 字典ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        // 字典为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            // 先删除字典项
            dictValueService.remove(QueryWrapper.create().in(DictValue::getDictId, idList));
            // 删除字典
            return this.removeByIds(idList);
        });
    }

}

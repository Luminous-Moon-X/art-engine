package com.art.service.impl;

import com.art.cache.RuleCache;
import com.art.cache.support.CacheRefreshService;
import com.art.domain.Rule;
import com.art.domain.vo.RuleItemVO;
import com.art.domain.vo.RuleVO;
import com.art.exception.ArtException;
import com.art.mapper.RuleMapper;
import com.art.service.RuleService;
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
 * 规则表服务实现类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
public class RuleServiceImpl extends ServiceImpl<RuleMapper, Rule> implements RuleService {

    /**
     * 规则缓存
     */
    private final RuleCache ruleCache;
    /**
     * 缓存刷新服务
     */
    private final CacheRefreshService cacheRefreshService;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param ruleCache           规则缓存
     * @param cacheRefreshService 缓存刷新服务
     * @param tenantSupport       多租户支持
     */
    public RuleServiceImpl(RuleCache ruleCache, CacheRefreshService cacheRefreshService, TenantSupport tenantSupport) {
        this.ruleCache = ruleCache;
        this.cacheRefreshService = cacheRefreshService;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 根据ID查询规则信息
     *
     * @param id 规则ID
     * @return 规则信息
     */
    @Override
    public Rule selectById(Long id) {
        // 规则为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.getById(id));
    }

    /**
     * 分页查询规则信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 规则信息
     */
    @Override
    public Page<RuleVO> queryPage(Page<RuleVO> page, RuleVO vo) {
        // 规则为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> {
            QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
            return this.getMapper().paginateAs(page, wrapper, RuleVO.class);
        });
    }

    /**
     * 查询所有规则信息
     *
     * @return 规则信息
     */
    @Override
    public List<RuleVO> selectList() {
        // 规则为系统级数据，不受租户过滤
        return tenantSupport.systemScope(() -> this.listAs(QueryWrapper.create(), RuleVO.class));
    }

    /**
     * 添加规则信息
     *
     * @param vo 规则信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(RuleVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Rule entity = ConvertUtil.convert(vo, Rule.class);
        // 规则为系统级数据，不受租户过滤
        boolean save = tenantSupport.systemScope(() -> this.save(entity));
        if (save) {
            cacheRefreshService.refreshAfterCommit(ruleCache);
        }
        return save;
    }

    /**
     * 编辑规则信息
     *
     * @param vo 规则信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(RuleVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        Rule entity = ConvertUtil.convert(vo, Rule.class);
        // 规则为系统级数据，不受租户过滤
        boolean edit = tenantSupport.systemScope(() -> this.updateById(entity));
        if (edit) {
            cacheRefreshService.refreshAfterCommit(ruleCache);
        }
        return edit;
    }

    /**
     * 删除规则信息
     *
     * @param idList 规则ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        // 规则为系统级数据，不受租户过滤
        boolean result = tenantSupport.systemScope(() -> this.removeByIds(idList));
        if (result) {
            cacheRefreshService.refreshAfterCommit(ruleCache);
        }
        return result;
    }

    /**
     * 根据编码获取规则信息
     *
     * @param code 规则编码
     * @return 规则信息
     */
    @Override
    public RuleItemVO getByRuleCode(String code) {
        return ruleCache.getByRuleCode(code);
    }
}

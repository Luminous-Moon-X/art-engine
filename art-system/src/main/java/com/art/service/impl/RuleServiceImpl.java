package com.art.service.impl;

import com.art.domain.Rule;
import com.art.domain.vo.RuleVO;
import com.art.exception.ArtException;
import com.art.mapper.RuleMapper;
import com.art.service.RuleService;
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
 * @since 0.0.1-SNAPSHOT
 */
@Service
public class RuleServiceImpl extends ServiceImpl<RuleMapper, Rule> implements RuleService {
    /**
     * 根据ID查询规则信息
     *
     * @param id 规则ID
     * @return 规则信息
     */
    @Override
    public Rule selectById(Long id) {
        return this.getById(id);
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
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        return this.getMapper().paginateAs(page, wrapper, RuleVO.class);
    }

    /**
     * 查询所有规则信息
     *
     * @return 规则信息
     */
    @Override
    public List<RuleVO> selectList() {
        return this.listAs(QueryWrapper.create(), RuleVO.class);
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
        return this.save(entity);
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
        return this.updateById(entity);
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
        return this.removeByIds(idList);
    }
}

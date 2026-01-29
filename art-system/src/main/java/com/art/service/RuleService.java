package com.art.service;

import com.art.domain.Rule;
import com.art.domain.vo.RuleItemVO;
import com.art.domain.vo.RuleVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 规则服务
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
public interface RuleService extends IService<Rule> {
    /**
     * 根据ID查询规则信息
     *
     * @param id 规则ID
     * @return 规则信息
     */
    Rule selectById(Long id);

    /**
     * 分页查询规则信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 规则信息
     */
    Page<RuleVO> queryPage(Page<RuleVO> page, RuleVO vo);

    /**
     * 查询所有规则信息
     *
     * @return 规则信息
     */
    List<RuleVO> selectList();

    /**
     * 添加规则信息
     *
     * @param vo 规则信息
     * @return 添加结果
     */
    Boolean add(RuleVO vo);

    /**
     * 编辑规则信息
     *
     * @param vo 规则信息
     * @return 编辑结果
     */
    Boolean edit(RuleVO vo);

    /**
     * 删除规则信息
     *
     * @param idList 规则ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 根据编码获取规则信息
     *
     * @param code 规则编码
     * @return 规则信息
     */
    RuleItemVO getByRuleCode(String code);
}

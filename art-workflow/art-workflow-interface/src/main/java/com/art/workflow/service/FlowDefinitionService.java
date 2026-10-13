package com.art.workflow.service;

import com.art.workflow.domain.vo.DefinitionOptionVO;
import com.art.workflow.domain.vo.DefinitionQueryVO;
import com.art.workflow.domain.vo.DefinitionVO;
import org.dromara.warm.flow.core.utils.page.Page;

import java.util.List;

/**
 * 流程定义服务
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public interface FlowDefinitionService {
    /**
     * 分页查询流程定义信息
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param vo       查询条件对象
     * @return 流程定义分页数据
     */
    Page<DefinitionVO> queryPage(Integer pageNum, Integer pageSize, DefinitionQueryVO vo);

    /**
     * 根据ID查询流程定义信息
     *
     * @param id 流程定义ID
     * @return 流程定义信息
     */
    DefinitionVO getDefinition(Long id);

    /**
     * 查询流程定义下拉选项
     *
     * @return 流程定义下拉选项
     */
    DefinitionOptionVO queryOptions();

    /**
     * 新增流程定义
     *
     * @param vo 流程定义信息
     * @return 新增结果
     */
    Boolean add(DefinitionVO vo);

    /**
     * 编辑流程定义
     *
     * @param vo 流程定义信息
     * @return 编辑结果
     */
    Boolean edit(DefinitionVO vo);

    /**
     * 删除流程定义
     *
     * @param idList 流程定义ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 发布流程定义（发布前会校验流程图是否完整）
     *
     * @param id 流程定义ID
     * @return 发布结果
     */
    Boolean publish(Long id);

    /**
     * 取消发布流程定义
     *
     * @param id 流程定义ID
     * @return 取消发布结果
     */
    Boolean unPublish(Long id);

    /**
     * 复制流程定义（新版本默认处于未发布状态）
     *
     * @param id 流程定义ID
     * @return 复制结果
     */
    Boolean copy(Long id);

    /**
     * 激活流程定义
     *
     * @param id 流程定义ID
     * @return 激活结果
     */
    Boolean active(Long id);

    /**
     * 挂起流程定义
     *
     * @param id 流程定义ID
     * @return 挂起结果
     */
    Boolean unActive(Long id);
}

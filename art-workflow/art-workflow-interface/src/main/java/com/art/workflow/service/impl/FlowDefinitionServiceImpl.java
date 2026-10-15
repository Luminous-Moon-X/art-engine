package com.art.workflow.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.art.exception.ArtException;
import com.art.utils.SecurityUtil;
import com.art.workflow.domain.vo.DefinitionOptionVO;
import com.art.workflow.domain.vo.DefinitionQueryVO;
import com.art.workflow.domain.vo.DefinitionVO;
import com.art.workflow.service.FlowDefinitionService;
import org.dromara.warm.flow.core.FlowEngine;
import org.dromara.warm.flow.core.entity.Definition;
import org.dromara.warm.flow.core.enums.ActivityStatus;
import org.dromara.warm.flow.core.enums.PublishStatus;
import org.dromara.warm.flow.core.service.DefService;
import org.dromara.warm.flow.core.utils.page.Page;
import org.dromara.warm.flow.orm.entity.FlowDefinition;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程定义服务实现类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
public class FlowDefinitionServiceImpl implements FlowDefinitionService {

    /**
     * 逻辑未删除标识（与 warm-flow 的 flow_definition.del_flag 语义保持一致）
     */
    private static final int DEL_FLAG_NORMAL = 0;

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE_NUM = 1;

    /**
     * 默认每页条数
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页条数
     */
    private static final int MAX_PAGE_SIZE = 1000;

    /**
     * warm-flow 流程定义服务
     */
    private final DefService defService;

    /**
     * 构造函数
     *
     * @param defService warm-flow 流程定义服务
     */
    public FlowDefinitionServiceImpl(DefService defService) {
        this.defService = defService;
    }

    /**
     * 分页查询流程定义信息
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param vo       查询条件对象
     * @return 流程定义分页数据
     */
    @Override
    public Page<DefinitionVO> queryPage(Integer pageNum, Integer pageSize, DefinitionQueryVO vo) {
        int current = (ObjectUtil.isNull(pageNum) || pageNum < 1) ? DEFAULT_PAGE_NUM : pageNum;
        int size = (ObjectUtil.isNull(pageSize) || pageSize < 1) ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
        DefinitionQueryVO queryVO = ObjectUtil.isNull(vo) ? new DefinitionQueryVO() : vo;

        // 流程名称/编码为模糊匹配，warm-flow 的实体查询不支持 LIKE，
        // 而流程定义属于低频配置类数据（单个租户下数量有限），故在内存中完成模糊过滤与分页
        List<DefinitionVO> matched = listByCondition(queryVO).stream()
                .filter(item -> StrUtil.isEmpty(queryVO.getFlowName())
                        || StrUtil.containsIgnoreCase(item.getFlowName(), queryVO.getFlowName()))
                .filter(item -> StrUtil.isEmpty(queryVO.getFlowCode())
                        || StrUtil.containsIgnoreCase(item.getFlowCode(), queryVO.getFlowCode()))
                .collect(Collectors.toList());

        int total = matched.size();
        int fromIndex = Math.min((current - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        Page<DefinitionVO> page = new Page<>(matched.subList(fromIndex, toIndex), total);
        page.setPageNum(current);
        page.setPageSize(size);
        return page;
    }

    /**
     * 根据ID查询流程定义信息
     *
     * @param id 流程定义ID
     * @return 流程定义信息
     */
    @Override
    public DefinitionVO getDefinition(Long id) {
        return this.toVO(this.getEntityById(id));
    }

    /**
     * 查询流程定义下拉选项
     *
     * @return 流程定义下拉选项
     */
    @Override
    public DefinitionOptionVO queryOptions() {
        DefinitionOptionVO optionVO = new DefinitionOptionVO();
        List<String> categoryList = defService.list(this.newEntity()).stream()
                .map(Definition::getCategory)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        optionVO.setCategoryList(categoryList);
        return optionVO;
    }

    /**
     * 新增流程定义
     *
     * @param vo 流程定义信息
     * @return 新增结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(DefinitionVO vo) {
        if (ObjectUtil.isNull(vo)) {
            throw new ArtException("数据为空，请检查！");
        }
        if (StrUtil.isBlank(vo.getFlowCode())) {
            throw new ArtException("流程编码不能为空！");
        }
        if (StrUtil.isBlank(vo.getFlowName())) {
            throw new ArtException("流程名称不能为空！");
        }
        this.checkFlowCodeUnique(vo.getFlowCode());

        FlowDefinition definition = this.newEntity();
        String currentUser = this.currentUser();
        definition.setFlowCode(vo.getFlowCode().trim());
        this.copyEditableFields(vo, definition);
        // 新增的流程定义默认为未发布、激活状态，逻辑未删除
        definition.setDelFlag(DEL_FLAG_NORMAL);
        definition.setIsPublish(PublishStatus.UNPUBLISHED.getKey());
        definition.setActivityStatus(ActivityStatus.ACTIVITY.getKey());
        definition.setTenantId(SecurityUtil.getTenantId());
        definition.setCreateBy(currentUser);
        definition.setUpdateBy(currentUser);

        // 由 warm-flow 生成自增版本号并落库（仅写入流程定义表，不含节点与跳转数据）
        return defService.checkAndSave(definition);
    }

    /**
     * 编辑流程定义<br/>
     * 流程编码、版本号、发布状态与激活状态由引擎与流程生命周期维护，此处不允许直接修改
     *
     * @param vo 流程定义信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(DefinitionVO vo) {
        if (ObjectUtil.isNull(vo) || ObjectUtil.isNull(vo.getId())) {
            throw new ArtException("数据为空，请检查！");
        }
        if (StrUtil.isBlank(vo.getFlowName())) {
            throw new ArtException("流程名称不能为空！");
        }
        FlowDefinition definition = this.getEntityById(vo.getId());
        this.copyEditableFields(vo, definition);
        definition.setUpdateBy(this.currentUser());
        return defService.updateById(definition);
    }

    /**
     * 删除流程定义（同时删除节点与跳转数据，已被流程实例使用时不允许删除）
     *
     * @param idList 流程定义ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        if (ObjectUtil.isEmpty(idList)) {
            throw new ArtException("请选择需要删除的流程定义！");
        }
        return defService.removeDef(idList);
    }

    /**
     * 发布流程定义
     *
     * @param id 流程定义ID
     * @return 发布结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publish(Long id) {
        this.getEntityById(id);
        return defService.publish(id);
    }

    /**
     * 取消发布流程定义
     *
     * @param id 流程定义ID
     * @return 取消发布结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unPublish(Long id) {
        this.getEntityById(id);
        return defService.unPublish(id);
    }

    /**
     * 复制流程定义
     *
     * @param id 流程定义ID
     * @return 复制结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean copy(Long id) {
        this.getEntityById(id);
        return defService.copyDef(id);
    }

    /**
     * 激活流程定义
     *
     * @param id 流程定义ID
     * @return 激活结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean active(Long id) {
        this.getEntityById(id);
        return defService.active(id);
    }

    /**
     * 挂起流程定义
     *
     * @param id 流程定义ID
     * @return 挂起结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unActive(Long id) {
        this.getEntityById(id);
        return defService.unActive(id);
    }

    /**
     * 按查询条件查询流程定义集合（不含模糊条件）
     *
     * @param vo 查询条件对象
     * @return 流程定义VO集合
     */
    private List<DefinitionVO> listByCondition(DefinitionQueryVO vo) {
        FlowDefinition entity = this.newEntity();
        if (ObjectUtil.isNotNull(vo)) {
            entity.setCategory(StrUtil.emptyToNull(StrUtil.trim(vo.getCategory())));
            entity.setIsPublish(vo.getIsPublish());
            entity.setActivityStatus(vo.getActivityStatus());
        }
        return defService.list(entity).stream()
                .map(this::toVO)
                .sorted(Comparator.comparing(definitionVO -> definitionVO != null ? definitionVO.getCreateTime() : null,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * 查询流程定义实体，不存在时抛出业务异常
     *
     * @param id 流程定义ID
     * @return 流程定义实体
     */
    private FlowDefinition getEntityById(Long id) {
        if (ObjectUtil.isNull(id)) {
            throw new ArtException("流程定义ID不能为空！");
        }
        FlowDefinition definition = (FlowDefinition) defService.getById(id);
        if (ObjectUtil.isNull(definition)) {
            throw new ArtException("流程定义不存在或已被删除！");
        }
        return definition;
    }

    /**
     * 校验流程编码是否重复<br/>
     * 流程编码为流程的唯一业务标识，新增后不可修改；同一编码下的多版本由「复制」操作产生
     *
     * @param flowCode 流程编码
     */
    private void checkFlowCodeUnique(String flowCode) {
        if (!defService.getByFlowCode(flowCode).isEmpty()) {
            throw new ArtException("流程编码【{}】已存在，请更换！", flowCode);
        }
    }

    /**
     * 复制允许前端维护的字段
     *
     * @param vo         前端提交的数据
     * @param definition 流程定义实体
     */
    private void copyEditableFields(DefinitionVO vo, FlowDefinition definition) {
        definition.setFlowName(StrUtil.trim(vo.getFlowName()));
        definition.setModelValue(StrUtil.emptyToNull(StrUtil.trim(vo.getModelValue())));
        definition.setCategory(StrUtil.emptyToNull(StrUtil.trim(vo.getCategory())));
        definition.setFormCustom(StrUtil.emptyToNull(StrUtil.trim(vo.getFormCustom())));
        definition.setFormPath(StrUtil.emptyToNull(StrUtil.trim(vo.getFormPath())));
        definition.setListenerType(StrUtil.emptyToNull(StrUtil.trim(vo.getListenerType())));
        definition.setListenerPath(StrUtil.emptyToNull(StrUtil.trim(vo.getListenerPath())));
        definition.setExt(StrUtil.emptyToNull(StrUtil.trim(vo.getExt())));
    }

    /**
     * 转换为流程定义VO
     *
     * @param definition 流程定义实体
     * @return 流程定义VO
     */
    private DefinitionVO toVO(Definition definition) {
        if (ObjectUtil.isNull(definition)) {
            return null;
        }
        DefinitionVO vo = new DefinitionVO();
        vo.setId(definition.getId());
        vo.setFlowCode(definition.getFlowCode());
        vo.setFlowName(definition.getFlowName());
        vo.setModelValue(definition.getModelValue());
        vo.setCategory(definition.getCategory());
        vo.setVersion(definition.getVersion());
        vo.setIsPublish(definition.getIsPublish());
        vo.setFormCustom(definition.getFormCustom());
        vo.setFormPath(definition.getFormPath());
        vo.setActivityStatus(definition.getActivityStatus());
        vo.setListenerType(definition.getListenerType());
        vo.setListenerPath(definition.getListenerPath());
        vo.setExt(definition.getExt());
        vo.setCreateBy(definition.getCreateBy());
        vo.setCreateTime(definition.getCreateTime());
        vo.setUpdateBy(definition.getUpdateBy());
        vo.setUpdateTime(definition.getUpdateTime());
        return vo;
    }

    /**
     * 创建流程定义实体（由 warm-flow 的实体工厂创建，保证与引擎使用的实现类型一致）
     *
     * @return 流程定义实体
     */
    private FlowDefinition newEntity() {
        return (FlowDefinition) FlowEngine.newDef();
    }

    /**
     * 获取当前登录用户标识（warm-flow 的创建人/更新人为字符串类型）
     *
     * @return 当前登录用户ID字符串，未登录时返回 null
     */
    private String currentUser() {
        Long userId = SecurityUtil.getUserId();
        return ObjectUtil.isNull(userId) ? null : userId.toString();
    }
}

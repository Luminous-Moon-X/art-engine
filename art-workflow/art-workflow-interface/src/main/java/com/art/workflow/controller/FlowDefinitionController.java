package com.art.workflow.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.enums.ApiOperationType;
import com.art.workflow.domain.vo.DefinitionOptionVO;
import com.art.workflow.domain.vo.DefinitionQueryVO;
import com.art.workflow.domain.vo.DefinitionVO;
import com.art.workflow.service.FlowDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.dromara.warm.flow.core.utils.page.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 流程定义控制器
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@RequestMapping("/workflow/definition")
@Tag(name = "流程定义管理", description = "流程定义管理相关接口")
public class FlowDefinitionController {

    /**
     * 流程定义服务
     */
    private final FlowDefinitionService flowDefinitionService;

    /**
     * 构造函数
     *
     * @param flowDefinitionService 流程定义服务
     */
    public FlowDefinitionController(FlowDefinitionService flowDefinitionService) {
        this.flowDefinitionService = flowDefinitionService;
    }

    /**
     * 分页查询流程定义信息
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param vo       查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询流程定义", description = "分页查询流程定义信息")
    public HttpResult<Page<DefinitionVO>> page(@RequestParam(value = "pageNumber", required = false) Integer pageNum,
                                               @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                               DefinitionQueryVO vo) {
        return HttpResult.success(this.flowDefinitionService.queryPage(pageNum, pageSize, vo));
    }

    /**
     * 根据ID获取流程定义信息
     *
     * @param id 流程定义ID
     * @return 流程定义信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取流程定义", description = "根据ID获取流程定义信息")
    public HttpResult<DefinitionVO> getById(@PathVariable("id") Long id) {
        return HttpResult.success(this.flowDefinitionService.getDefinition(id));
    }

    /**
     * 查询流程定义下拉选项
     *
     * @return 流程定义下拉选项
     */
    @GetMapping("/options")
    @Operation(summary = "查询流程定义下拉选项", description = "查询流程类别等下拉选项")
    public HttpResult<DefinitionOptionVO> options() {
        return HttpResult.success(this.flowDefinitionService.queryOptions());
    }

    /**
     * 新增流程定义
     *
     * @param vo 流程定义信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增流程定义", description = "新增流程定义（仅流程定义表数据，流程图后续在设计器中维护）")
    @ApiLog(module = "流程定义管理", operationType = ApiOperationType.INSERT, description = "新增流程定义")
    public HttpResult<Boolean> add(@RequestBody DefinitionVO vo) {
        return HttpResult.success(this.flowDefinitionService.add(vo));
    }

    /**
     * 编辑流程定义
     *
     * @param vo 流程定义信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑流程定义", description = "编辑流程定义（流程编码、版本、发布状态不可修改）")
    @ApiLog(module = "流程定义管理", operationType = ApiOperationType.UPDATE, description = "编辑流程定义")
    public HttpResult<Boolean> edit(@RequestBody DefinitionVO vo) {
        return HttpResult.success(this.flowDefinitionService.edit(vo));
    }

    /**
     * 删除流程定义
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除流程定义", description = "根据ID删除流程定义（同时删除节点与跳转数据）")
    @ApiLog(module = "流程定义管理", operationType = ApiOperationType.DELETE, description = "删除流程定义")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.flowDefinitionService.delete(tableRowVO.getIdList()));
    }

    /**
     * 发布流程定义
     *
     * @param id 流程定义ID
     * @return 发布结果
     */
    @PutMapping("/publish")
    @Operation(summary = "发布流程定义", description = "发布流程定义（需已在设计器中完成流程图配置）")
    @ApiLog(module = "流程定义管理", operationType = ApiOperationType.UPDATE, description = "发布流程定义")
    public HttpResult<Boolean> publish(@RequestParam("id") @NotNull Long id) {
        return HttpResult.success(this.flowDefinitionService.publish(id));
    }

    /**
     * 取消发布流程定义
     *
     * @param id 流程定义ID
     * @return 取消发布结果
     */
    @PutMapping("/un-publish")
    @Operation(summary = "取消发布流程定义", description = "取消发布流程定义（已产生流程实例时不允许取消发布）")
    @ApiLog(module = "流程定义管理", operationType = ApiOperationType.UPDATE, description = "取消发布流程定义")
    public HttpResult<Boolean> unPublish(@RequestParam("id") @NotNull Long id) {
        return HttpResult.success(this.flowDefinitionService.unPublish(id));
    }

    /**
     * 复制流程定义
     *
     * @param id 流程定义ID
     * @return 复制结果
     */
    @PostMapping("/copy")
    @Operation(summary = "复制流程定义", description = "复制流程定义及其节点、跳转数据，生成新版本（未发布）")
    @ApiLog(module = "流程定义管理", operationType = ApiOperationType.INSERT, description = "复制流程定义")
    public HttpResult<Boolean> copy(@RequestParam("id") @NotNull Long id) {
        return HttpResult.success(this.flowDefinitionService.copy(id));
    }

    /**
     * 激活流程定义
     *
     * @param id 流程定义ID
     * @return 激活结果
     */
    @PutMapping("/active")
    @Operation(summary = "激活流程定义", description = "激活流程定义")
    @ApiLog(module = "流程定义管理", operationType = ApiOperationType.UPDATE, description = "激活流程定义")
    public HttpResult<Boolean> active(@RequestParam("id") @NotNull Long id) {
        return HttpResult.success(this.flowDefinitionService.active(id));
    }

    /**
     * 挂起流程定义
     *
     * @param id 流程定义ID
     * @return 挂起结果
     */
    @PutMapping("/un-active")
    @Operation(summary = "挂起流程定义", description = "挂起流程定义（挂起后相关流程实例无法继续流转）")
    @ApiLog(module = "流程定义管理", operationType = ApiOperationType.UPDATE, description = "挂起流程定义")
    public HttpResult<Boolean> unActive(@RequestParam("id") @NotNull Long id) {
        return HttpResult.success(this.flowDefinitionService.unActive(id));
    }
}

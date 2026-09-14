package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.Rule;
import com.art.domain.vo.RuleItemVO;
import com.art.domain.vo.RuleVO;
import com.art.enums.ApiOperationType;
import com.art.exception.ArtException;
import com.art.service.RuleService;
import com.art.tenant.TenantSupport;
import com.art.utils.SecurityUtil;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 规则控制器
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@RequestMapping("/rule")
@Tag(name = "规则管理", description = "规则管理相关接口")
public class RuleController {
    /**
     * 规则服务
     */
    private final RuleService RuleService;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param RuleService   规则服务
     * @param tenantSupport 多租户支持
     */
    public RuleController(RuleService RuleService, TenantSupport tenantSupport) {
        this.RuleService = RuleService;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 根据ID获取规则信息
     *
     * @param id 规则ID
     * @return 规则信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取规则信息", description = "根据ID获取规则信息（仅超级管理员）")
    public HttpResult<Rule> getById(@PathVariable("id") Long id) {
        this.checkSuperAdmin();
        return HttpResult.success(this.RuleService.selectById(id));
    }

    /**
     * 根据编码获取规则信息（业务读取口，所有登录用户可用）
     *
     * @param code 规则编码
     * @return 规则信息
     */
    @GetMapping("/code/{code}")
    @Operation(summary = "根据编码获取规则信息", description = "根据编码获取规则信息")
    public HttpResult<RuleItemVO> getByCode(@PathVariable("code") String code) {
        return HttpResult.success(this.RuleService.getByRuleCode(code));
    }

    /**
     * 分页查询规则信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询规则信息", description = "分页查询规则信息（仅超级管理员）")
    public HttpResult<Page<RuleVO>> page(Page<RuleVO> page, RuleVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(this.RuleService.queryPage(page, vo));
    }

    /**
     * 查询所有规则信息
     *
     * @return 规则信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有规则信息", description = "查询所有规则信息（仅超级管理员）")
    public HttpResult<List<RuleVO>> list() {
        this.checkSuperAdmin();
        return HttpResult.success(this.RuleService.selectList());
    }

    /**
     * 新增规则
     *
     * @param vo 规则信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增规则", description = "新增规则（仅超级管理员）")
    @ApiLog(module = "规则管理", operationType = ApiOperationType.INSERT, description = "新增规则")
    public HttpResult<Boolean> add(@RequestBody RuleVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(RuleService.add(vo));
    }

    /**
     * 编辑规则
     *
     * @param vo 规则信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑规则", description = "编辑规则（仅超级管理员）")
    @ApiLog(module = "规则管理", operationType = ApiOperationType.UPDATE, description = "编辑规则")
    public HttpResult<Boolean> edit(@RequestBody RuleVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(RuleService.edit(vo));
    }

    /**
     * 根据ID删除规则
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除规则", description = "根据ID删除规则（仅超级管理员）")
    @ApiLog(module = "规则管理", operationType = ApiOperationType.DELETE, description = "删除规则")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        this.checkSuperAdmin();
        return HttpResult.success(this.RuleService.delete(tableRowVO.getIdList()));
    }

    /**
     * 校验当前用户是否为超级管理员<br/>
     * 规则为系统级共享数据，规则管理仅允许超级管理员操作
     */
    private void checkSuperAdmin() {
        if (!tenantSupport.isSuperAdmin(SecurityUtil.getUserType())) {
            throw new ArtException("仅超级管理员可以操作规则管理！");
        }
    }
}

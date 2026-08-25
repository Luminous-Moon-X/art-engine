package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.Tenant;
import com.art.domain.vo.TenantVO;
import com.art.enums.ApiOperationType;
import com.art.service.TenantService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户控制器
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@RequestMapping("/tenant")
@Tag(name = "租户管理", description = "租户管理相关接口")
public class TenantController {
    /**
     * 租户服务
     */
    private final TenantService tenantService;

    /**
     * 构造函数
     *
     * @param tenantService 租户服务
     */
    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    /**
     * 根据ID获取租户信息
     *
     * @param id 租户ID
     * @return 租户信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取租户信息", description = "根据ID获取租户信息")
    public HttpResult<Tenant> getById(@PathVariable("id") Long id) {
        return HttpResult.success(this.tenantService.selectById(id));
    }

    /**
     * 分页查询租户信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询租户信息", description = "分页查询租户信息")
    public HttpResult<Page<Tenant>> page(Page<Tenant> page, TenantVO vo) {
        return HttpResult.success(this.tenantService.queryPage(page, vo));
    }

    /**
     * 查询所有租户信息
     *
     * @return 租户信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有租户信息", description = "查询所有租户信息")
    public HttpResult<List<Tenant>> list() {
        return HttpResult.success(this.tenantService.selectList());
    }

    /**
     * 新增租户
     *
     * @param vo 租户信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增租户", description = "新增租户")
    @ApiLog(module = "租户管理", operationType = ApiOperationType.INSERT, description = "新增租户")
    public HttpResult<Boolean> add(@RequestBody TenantVO vo) {
        return HttpResult.success(tenantService.add(vo));
    }

    /**
     * 编辑租户
     *
     * @param vo 租户信息
     * @return 编辑结果
     */
    @PostMapping("/edit")
    @Operation(summary = "编辑租户", description = "编辑租户")
    @ApiLog(module = "租户管理", operationType = ApiOperationType.UPDATE, description = "编辑租户")
    public HttpResult<Boolean> edit(@RequestBody TenantVO vo) {
        return HttpResult.success(tenantService.edit(vo));
    }

    /**
     * 根据ID删除租户
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除租户", description = "根据ID删除租户")
    @ApiLog(module = "租户管理", operationType = ApiOperationType.DELETE, description = "删除租户")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.tenantService.delete(tableRowVO.getIdList()));
    }

    /**
     * 启用/禁用租户
     *
     * @param id 租户ID
     * @return 操作结果
     */
    @PutMapping("/toggle/{id}")
    @Operation(summary = "启用/禁用租户", description = "切换租户启用状态")
    @ApiLog(module = "租户管理", operationType = ApiOperationType.UPDATE, description = "启用/禁用租户")
    public HttpResult<Boolean> toggleEnable(@PathVariable("id") Long id) {
        return HttpResult.success(this.tenantService.toggleEnable(id));
    }
}

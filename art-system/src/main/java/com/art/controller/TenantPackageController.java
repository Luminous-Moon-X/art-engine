package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.SelectVO;
import com.art.common.TableRowVO;
import com.art.domain.vo.TenantPackageVO;
import com.art.enums.ApiOperationType;
import com.art.exception.ArtException;
import com.art.service.TenantPackageService;
import com.art.tenant.TenantSupport;
import com.art.utils.SecurityUtil;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租户套餐信息接口（仅超级管理员）
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@RestController
@AllArgsConstructor
@RequestMapping("/tenantPackage")
public class TenantPackageController {
    /**
     * 租户套餐服务
     */
    private final TenantPackageService tenantPackageService;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 分页查询租户套餐信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 租户套餐信息
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询租户套餐信息", description = "分页查询租户套餐信息（仅超级管理员）")
    public HttpResult<Page<TenantPackageVO>> page(Page<TenantPackageVO> page, TenantPackageVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantPackageService.queryPage(page, vo));
    }

    /**
     * 查询启用的租户套餐下拉列表
     *
     * @return 租户套餐下拉列表
     */
    @GetMapping("/select")
    @Operation(summary = "查询租户套餐下拉列表", description = "查询启用中的租户套餐下拉列表（仅超级管理员）")
    public HttpResult<List<SelectVO>> select() {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantPackageService.select());
    }

    /**
     * 根据ID查询租户套餐信息
     *
     * @param id 租户套餐ID
     * @return 租户套餐信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询租户套餐信息", description = "根据ID查询租户套餐信息（仅超级管理员）")
    public HttpResult<TenantPackageVO> getById(@PathVariable("id") Long id) {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantPackageService.selectById(id));
    }

    /**
     * 新增租户套餐
     *
     * @param vo 租户套餐信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增租户套餐", description = "新增租户套餐（仅超级管理员）")
    @ApiLog(module = "租户套餐管理", operationType = ApiOperationType.INSERT, description = "新增租户套餐")
    public HttpResult<Boolean> add(@RequestBody TenantPackageVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantPackageService.add(vo));
    }

    /**
     * 编辑租户套餐
     *
     * @param vo 租户套餐信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑租户套餐", description = "编辑租户套餐（仅超级管理员）")
    @ApiLog(module = "租户套餐管理", operationType = ApiOperationType.UPDATE, description = "编辑租户套餐")
    public HttpResult<Boolean> edit(@RequestBody TenantPackageVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantPackageService.edit(vo));
    }

    /**
     * 删除租户套餐
     *
     * @param tableRowVO 表格行VO
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除租户套餐", description = "删除租户套餐（仅超级管理员）")
    @ApiLog(module = "租户套餐管理", operationType = ApiOperationType.DELETE, description = "删除租户套餐")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantPackageService.delete(tableRowVO.getIdList()));
    }

    /**
     * 校验当前用户是否为超级管理员
     */
    private void checkSuperAdmin() {
        if (!tenantSupport.isSuperAdmin(SecurityUtil.getUserType())) {
            throw new ArtException("仅超级管理员可以操作租户套餐管理！");
        }
    }
}
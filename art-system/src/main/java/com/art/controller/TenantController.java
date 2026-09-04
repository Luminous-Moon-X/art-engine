package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.vo.TenantVO;
import com.art.enums.ApiOperationType;
import com.art.exception.ArtException;
import com.art.service.TenantService;
import com.art.tenant.TenantSupport;
import com.art.utils.SecurityUtil;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户信息接口
 *
 * @author Luminous.X
 * @since 2.0.0
 */
@RestController
@AllArgsConstructor
@RequestMapping("/tenant")
public class TenantController {
    /**
     * 租户服务
     */
    private final TenantService tenantService;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 获取多租户配置（登录页使用，无需登录）
     *
     * @return 多租户配置
     */
    @GetMapping("/config")
    @Operation(summary = "获取多租户配置", description = "获取多租户配置，登录页根据开关决定是否展示租户下拉框")
    public HttpResult<Map<String, Object>> config() {
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("tenantEnable", tenantSupport.isEnable());
        configMap.put("defaultTenantId", tenantSupport.getDefaultTenantId());
        return HttpResult.success(configMap);
    }

    /**
     * 查询启用中的租户列表（登录页租户下拉框、超级管理员切换租户，无需登录）
     *
     * @return 租户列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询启用中的租户列表", description = "查询启用中的租户列表，登录页租户下拉框使用")
    public HttpResult<List<TenantVO>> list() {
        return HttpResult.success(this.tenantService.listEnabled());
    }

    /**
     * 分页查询租户信息（仅超级管理员）
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 租户信息
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询租户信息", description = "分页查询租户信息（仅超级管理员）")
    public HttpResult<Page<TenantVO>> page(Page<TenantVO> page, TenantVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantService.queryPage(page, vo));
    }

    /**
     * 新增租户（同时创建租户管理员，仅超级管理员）
     *
     * @param vo 租户信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增租户", description = "新增租户并同时创建该租户的管理员账号（仅超级管理员）")
    @ApiLog(module = "租户管理", operationType = ApiOperationType.INSERT, description = "新增租户")
    public HttpResult<Boolean> add(@RequestBody TenantVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantService.add(vo));
    }

    /**
     * 编辑租户（仅超级管理员）
     *
     * @param vo 租户信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑租户", description = "编辑租户信息（仅超级管理员）")
    @ApiLog(module = "租户管理", operationType = ApiOperationType.UPDATE, description = "编辑租户")
    public HttpResult<Boolean> edit(@RequestBody TenantVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantService.edit(vo));
    }

    /**
     * 删除租户（仅超级管理员）
     *
     * @param tableRowVO 表格行VO
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除租户", description = "删除租户信息（仅超级管理员）")
    @ApiLog(module = "租户管理", operationType = ApiOperationType.DELETE, description = "删除租户")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        this.checkSuperAdmin();
        return HttpResult.success(this.tenantService.delete(tableRowVO.getIdList()));
    }

    /**
     * 切换当前登录用户的生效租户（仅超级管理员）
     *
     * @param tenantId 目标租户ID
     * @return 切换结果
     */
    @PostMapping("/switch")
    @Operation(summary = "切换租户", description = "超级管理员切换当前生效租户")
    @ApiLog(module = "租户管理", operationType = ApiOperationType.UPDATE, description = "切换租户")
    public HttpResult<Boolean> switchTenant(@RequestParam("tenantId") Long tenantId) {
        return HttpResult.success(this.tenantService.switchTenant(tenantId));
    }

    /**
     * 校验当前用户是否为超级管理员
     */
    private void checkSuperAdmin() {
        if (!tenantSupport.isSuperAdmin(SecurityUtil.getUserType())) {
            throw new ArtException("仅超级管理员可以操作租户管理！");
        }
    }
}
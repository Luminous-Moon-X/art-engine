package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.DictValue;
import com.art.domain.vo.DictItemVO;
import com.art.domain.vo.DictValueVO;
import com.art.enums.ApiOperationType;
import com.art.exception.ArtException;
import com.art.service.DictValueService;
import com.art.tenant.TenantSupport;
import com.art.utils.SecurityUtil;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 字典值控制器
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dictValue")
@Tag(name = "字典值管理", description = "字典值管理相关接口")
public class DictValueController {
    /**
     * 字典值服务
     */
    private final DictValueService DictValueService;
    /**
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param DictValueService 字典值服务
     * @param tenantSupport    多租户支持
     */
    public DictValueController(DictValueService DictValueService, TenantSupport tenantSupport) {
        this.DictValueService = DictValueService;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 根据ID获取字典值信息
     *
     * @param id 字典值ID
     * @return 字典值信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取字典值信息", description = "根据ID获取字典值信息（仅超级管理员）")
    public HttpResult<DictValue> getById(@PathVariable("id") Long id) {
        this.checkSuperAdmin();
        return HttpResult.success(this.DictValueService.selectById(id));
    }

    /**
     * 分页查询字典值信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询字典值信息", description = "分页查询字典值信息（仅超级管理员）")
    public HttpResult<Page<DictValueVO>> page(Page<DictValueVO> page, DictValueVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(this.DictValueService.queryPage(page, vo));
    }

    /**
     * 查询所有字典值信息
     *
     * @return 字典值信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有字典值信息", description = "查询所有字典值信息（仅超级管理员）")
    public HttpResult<List<DictValue>> list() {
        this.checkSuperAdmin();
        return HttpResult.success(this.DictValueService.selectList());
    }

    /**
     * 新增字典值
     *
     * @param vo 字典值信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增字典值", description = "新增字典值（仅超级管理员）")
    @ApiLog(module = "字典值管理", operationType = ApiOperationType.INSERT, description = "新增字典值")
    public HttpResult<Boolean> add(@RequestBody DictValueVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(DictValueService.add(vo));
    }

    /**
     * 编辑字典值
     *
     * @param vo 字典值信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑字典值", description = "编辑字典值（仅超级管理员）")
    @ApiLog(module = "字典值管理", operationType = ApiOperationType.UPDATE, description = "编辑字典值")
    public HttpResult<Boolean> edit(@RequestBody DictValueVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(DictValueService.edit(vo));
    }

    /**
     * 根据ID删除字典值
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除字典值", description = "根据ID删除字典值（仅超级管理员）")
    @ApiLog(module = "字典值管理", operationType = ApiOperationType.DELETE, description = "删除字典值")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        this.checkSuperAdmin();
        return HttpResult.success(this.DictValueService.delete(tableRowVO.getIdList()));
    }

    /**
     * 根据字典编码查询字典值（业务读取口，所有登录用户可用）
     *
     * @param dictCode 字典编码
     * @return 字典值列表
     */
    @GetMapping("/dictByCode/{dictCode}")
    @Operation(summary = "根据字典编码查询字典值", description = "根据字典编码查询字典值")
    public HttpResult<List<DictItemVO>> dictByCode(@PathVariable("dictCode") String dictCode) {
        return HttpResult.success(this.DictValueService.dictByCode(dictCode));
    }

    /**
     * 校验当前用户是否为超级管理员<br/>
     * 字典值为系统级共享数据，字典值管理仅允许超级管理员操作
     */
    private void checkSuperAdmin() {
        if (!tenantSupport.isSuperAdmin(SecurityUtil.getUserType())) {
            throw new ArtException("仅超级管理员可以操作字典值管理！");
        }
    }
}

package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.vo.OssConfigVO;
import com.art.enums.ApiOperationType;
import com.art.service.OssConfigService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 对象存储配置控制器。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@RestController
@RequestMapping("/oss/config")
@Tag(name = "对象存储配置管理", description = "对象存储配置管理相关接口")
public class OssConfigController {

    /**
     * 对象存储配置服务
     */
    private final OssConfigService ossConfigService;

    /**
     * 构造函数。
     *
     * @param ossConfigService 对象存储配置服务
     */
    public OssConfigController(OssConfigService ossConfigService) {
        this.ossConfigService = ossConfigService;
    }

    /**
     * 根据ID获取对象存储配置。
     *
     * @param id 配置ID
     * @return 配置信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取对象存储配置", description = "根据ID获取对象存储配置")
    public HttpResult<OssConfigVO> getById(@PathVariable("id") Long id) {
        return HttpResult.success(this.ossConfigService.getDetail(id));
    }

    /**
     * 分页查询对象存储配置。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询对象存储配置", description = "分页查询对象存储配置")
    public HttpResult<Page<OssConfigVO>> page(Page<OssConfigVO> page, OssConfigVO vo) {
        return HttpResult.success(this.ossConfigService.queryPage(page, vo));
    }

    /**
     * 查询全部对象存储配置。
     *
     * @return 配置列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询全部对象存储配置", description = "查询全部对象存储配置")
    public HttpResult<List<OssConfigVO>> list() {
        return HttpResult.success(this.ossConfigService.selectList());
    }

    /**
     * 新增对象存储配置。
     *
     * @param vo 配置信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增对象存储配置", description = "新增对象存储配置")
    @ApiLog(module = "对象存储配置", operationType = ApiOperationType.INSERT, description = "新增对象存储配置")
    public HttpResult<Boolean> add(@RequestBody OssConfigVO vo) {
        return HttpResult.success(this.ossConfigService.add(vo));
    }

    /**
     * 编辑对象存储配置。
     *
     * @param vo 配置信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑对象存储配置", description = "编辑对象存储配置")
    @ApiLog(module = "对象存储配置", operationType = ApiOperationType.UPDATE, description = "编辑对象存储配置")
    public HttpResult<Boolean> edit(@RequestBody OssConfigVO vo) {
        return HttpResult.success(this.ossConfigService.edit(vo));
    }

    /**
     * 启用对象存储配置。
     *
     * @param id 配置ID
     * @return 启用结果
     */
    @PutMapping("/enable/{id}")
    @Operation(summary = "启用对象存储配置", description = "启用对象存储配置")
    @ApiLog(module = "对象存储配置", operationType = ApiOperationType.UPDATE, description = "启用对象存储配置")
    public HttpResult<Boolean> enable(@PathVariable("id") Long id) {
        return HttpResult.success(this.ossConfigService.enable(id));
    }

    /**
     * 删除对象存储配置。
     *
     * @param tableRowVO 表格行VO
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除对象存储配置", description = "删除对象存储配置")
    @ApiLog(module = "对象存储配置", operationType = ApiOperationType.DELETE, description = "删除对象存储配置")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.ossConfigService.delete(tableRowVO.getIdList()));
    }
}

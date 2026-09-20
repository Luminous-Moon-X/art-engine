package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.vo.PermissionRowVO;
import com.art.domain.vo.PermissionTableColumnVO;
import com.art.domain.vo.PermissionTableVO;
import com.art.enums.ApiOperationType;
import com.art.service.PermissionRowService;
import com.art.service.TableMetadataService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 数据权限控制器
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@RestController
@RequestMapping("/permission/row")
@Tag(name = "数据权限管理", description = "数据权限管理相关接口")
public class PermissionRowController {

    /**
     * 数据权限服务
     */
    private final PermissionRowService permissionRowService;

    /**
     * 数据库表元数据服务
     */
    private final TableMetadataService tableMetadataService;

    /**
     * 构造函数
     *
     * @param permissionRowService  数据权限服务
     * @param tableMetadataService  数据库表元数据服务
     */
    public PermissionRowController(PermissionRowService permissionRowService,
                                   TableMetadataService tableMetadataService) {
        this.permissionRowService = permissionRowService;
        this.tableMetadataService = tableMetadataService;
    }

    /**
     * 查询数据库中的所有表（含表备注）<br/>
     * 用于数据权限「授权客体」下拉
     *
     * @return 表信息列表
     */
    @GetMapping("/table/list")
    @Operation(summary = "查询数据库所有表", description = "查询数据库所有表（含表备注），用于授权客体下拉")
    public HttpResult<List<PermissionTableVO>> tableList() {
        return HttpResult.success(this.tableMetadataService.selectTableList());
    }

    /**
     * 查询指定表的字段列表（含字段备注）<br/>
     * 用于数据权限「权限字段」下拉
     *
     * @param tableName 表名
     * @return 字段信息列表
     */
    @GetMapping("/table/{tableName}/column/list")
    @Operation(summary = "查询指定表的字段列表", description = "查询指定表的字段列表（含字段备注），用于权限字段下拉")
    public HttpResult<List<PermissionTableColumnVO>> tableColumnList(@PathVariable("tableName") String tableName) {
        return HttpResult.success(this.tableMetadataService.selectTableColumnList(tableName));
    }

    /**
     * 分页查询数据权限信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询数据权限信息", description = "分页查询数据权限信息（自动限定当前租户）")
    public HttpResult<Page<PermissionRowVO>> page(Page<PermissionRowVO> page, PermissionRowVO vo) {
        return HttpResult.success(this.permissionRowService.queryPage(page, vo));
    }

    /**
     * 新增数据权限
     *
     * @param vo 数据权限信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增数据权限", description = "新增数据权限（自动归属当前租户）")
    @ApiLog(module = "数据权限管理", operationType = ApiOperationType.INSERT, description = "新增数据权限")
    public HttpResult<Boolean> add(@RequestBody PermissionRowVO vo) {
        return HttpResult.success(this.permissionRowService.add(vo));
    }

    /**
     * 编辑数据权限
     *
     * @param vo 数据权限信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑数据权限", description = "编辑数据权限（自动限定当前租户）")
    @ApiLog(module = "数据权限管理", operationType = ApiOperationType.UPDATE, description = "编辑数据权限")
    public HttpResult<Boolean> edit(@RequestBody PermissionRowVO vo) {
        return HttpResult.success(this.permissionRowService.edit(vo));
    }

    /**
     * 启用/禁用数据权限
     *
     * @param id         数据权限ID
     * @param enableFlag 是否启用
     * @return 操作结果
     */
    @PutMapping("/status/{id}")
    @Operation(summary = "启用/禁用数据权限", description = "启用/禁用数据权限（自动限定当前租户）")
    @ApiLog(module = "数据权限管理", operationType = ApiOperationType.UPDATE, description = "启用/禁用数据权限")
    public HttpResult<Boolean> updateStatus(@PathVariable("id") Long id,
                                            @RequestParam("enableFlag") Boolean enableFlag) {
        return HttpResult.success(this.permissionRowService.updateStatus(id, enableFlag));
    }

    /**
     * 删除数据权限
     *
     * @param tableRowVO 表格行VO
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除数据权限", description = "删除数据权限（自动限定当前租户）")
    @ApiLog(module = "数据权限管理", operationType = ApiOperationType.DELETE, description = "删除数据权限")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.permissionRowService.delete(tableRowVO.getIdList()));
    }
}

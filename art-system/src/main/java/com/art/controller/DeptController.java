package com.art.controller;

import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.Dept;
import com.art.domain.vo.DeptTreeSelectVO;
import com.art.domain.vo.DeptVO;
import com.art.service.DeptService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门控制器
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@RestController
@RequestMapping("/dept")
@Tag(name = "部门管理", description = "部门管理相关接口")
public class DeptController {
    /**
     * 部门服务
     */
    private final DeptService DeptService;

    /**
     * 构造函数
     *
     * @param DeptService 部门服务
     */
    public DeptController(DeptService DeptService) {
        this.DeptService = DeptService;
    }

    /**
     * 根据ID获取部门信息
     *
     * @param id 部门ID
     * @return 部门信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取部门信息", description = "根据ID获取部门信息")
    public HttpResult<Dept> getById(@PathVariable("id") Long id) {
        return HttpResult.success(this.DeptService.selectById(id));
    }

    /**
     * 分页查询部门信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询部门信息", description = "分页查询部门信息")
    public HttpResult<Page<DeptVO>> page(Page<DeptVO> page, DeptVO vo) {
        return HttpResult.success(this.DeptService.queryPage(page, vo));
    }

    /**
     * 查询所有部门信息
     *
     * @return 部门信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有部门信息", description = "查询所有部门信息")
    public HttpResult<List<DeptVO>> list() {
        return HttpResult.success(this.DeptService.selectList());
    }

    /**
     * 部门树形下拉列表
     *
     * @return 部门树形下拉列表
     */
    @GetMapping("/treeSelect")
    @Operation(summary = "部门树形下拉列表", description = "部门树形下拉列表")
    public HttpResult<List<DeptTreeSelectVO>> treeSelect() {
        return HttpResult.success(this.DeptService.treeSelect());
    }

    /**
     * 部门树形下拉列表(不包含顶级节点)
     *
     * @return 部门树形下拉列表
     */
    @GetMapping("/treeSelectNoTop")
    @Operation(summary = "部门树形下拉列表(不包含顶级节点)", description = "部门树形下拉列表(不包含顶级节点)")
    public HttpResult<List<DeptTreeSelectVO>> treeSelectNoTop() {
        return HttpResult.success(this.DeptService.treeSelectNoTop());
    }


    /**
     * 新增部门
     *
     * @param vo 部门信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增部门", description = "新增部门")
    public HttpResult<Boolean> add(@RequestBody DeptVO vo) {
        return HttpResult.success(DeptService.add(vo));
    }

    /**
     * 编辑部门
     *
     * @param vo 部门信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑部门", description = "编辑部门")
    public HttpResult<Boolean> edit(@RequestBody DeptVO vo) {
        return HttpResult.success(DeptService.edit(vo));
    }

    /**
     * 根据ID删除部门
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除部门", description = "根据ID删除部门")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.DeptService.delete(tableRowVO.getIdList()));
    }

}

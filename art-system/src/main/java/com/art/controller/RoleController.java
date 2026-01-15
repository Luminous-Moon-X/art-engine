package com.art.controller;

import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.Role;
import com.art.domain.vo.RoleVO;
import com.art.service.RoleService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@RestController
@RequestMapping("/role")
@Tag(name = "角色管理", description = "角色管理相关接口")
public class RoleController {
    /**
     * 角色服务
     */
    private final RoleService roleService;

    /**
     * 构造函数
     *
     * @param roleService 角色服务
     */
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * 根据ID获取角色信息
     *
     * @param id 角色ID
     * @return 角色信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取角色信息", description = "根据ID获取角色信息")
    public HttpResult<Role> getById(@PathVariable("id") Long id) {
        return HttpResult.success(this.roleService.selectById(id));
    }

    /**
     * 分页查询角色信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询角色信息", description = "分页查询角色信息")
    public HttpResult<Page<Role>> page(Page<Role> page, RoleVO vo) {
        return HttpResult.success(this.roleService.queryPage(page, vo));
    }

    /**
     * 查询所有角色信息
     *
     * @return 角色信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有角色信息", description = "查询所有角色信息")
    public HttpResult<List<Role>> list() {
        return HttpResult.success(this.roleService.selectList());
    }

    /**
     * 新增角色
     *
     * @param vo 角色信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增角色", description = "新增角色")
    public HttpResult<Boolean> add(@RequestBody RoleVO vo) {
        return HttpResult.success(roleService.add(vo));
    }

    /**
     * 编辑角色
     *
     * @param vo 角色信息
     * @return 编辑结果
     */
    @PostMapping("/edit")
    @Operation(summary = "编辑角色", description = "编辑角色")
    public HttpResult<Boolean> edit(@RequestBody RoleVO vo) {
        return HttpResult.success(roleService.edit(vo));
    }

    /**
     * 根据ID删除角色
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除角色", description = "根据ID删除角色")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.roleService.delete(tableRowVO.getIdList()));
    }

}
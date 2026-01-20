package com.art.controller;

import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.domain.Menu;
import com.art.domain.vo.MenuTreeVO;
import com.art.domain.vo.MenuVO;
import com.art.service.MenuService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuController {
    /**
     * 菜单服务
     */
    private final MenuService menuService;

    /**
     * 构造函数
     *
     * @param menuService 菜单服务
     */
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 根据ID获取菜单信息
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取菜单信息", description = "根据ID获取菜单信息")
    public HttpResult<MenuVO> getById(@PathVariable("id") Long id) {
        return HttpResult.success(this.menuService.selectById(id));
    }

    /**
     * 分页查询菜单信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 分页数据对象
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询菜单信息", description = "分页查询菜单信息")
    public HttpResult<Page<MenuVO>> page(Page<MenuVO> page, MenuVO vo) {
        return HttpResult.success(this.menuService.queryPage(page, vo));
    }

    /**
     * 查询所有菜单信息
     *
     * @return 菜单信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有菜单信息", description = "查询所有菜单信息")
    public HttpResult<List<Menu>> list() {
        return HttpResult.success(this.menuService.selectList());
    }

    /**
     * 新增菜单
     *
     * @param vo 菜单信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增菜单", description = "新增菜单")
    public HttpResult<Boolean> add(@RequestBody MenuVO vo) {
        return HttpResult.success(menuService.add(vo));
    }

    /**
     * 编辑菜单
     *
     * @param vo 菜单信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑菜单", description = "编辑菜单")
    public HttpResult<Boolean> edit(@RequestBody MenuVO vo) {
        return HttpResult.success(menuService.edit(vo));
    }

    /**
     * 根据ID删除菜单
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除菜单", description = "根据ID删除菜单")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        return HttpResult.success(this.menuService.delete(tableRowVO.getIdList()));
    }

    /**
     * 获取菜单树
     *
     * @return 菜单树
     */
    @GetMapping("/menuTree")
    @Operation(summary = "获取菜单树", description = "获取菜单树，用于渲染前端菜单栏")
    public HttpResult<List<MenuTreeVO>> menuTree() {
        return HttpResult.success(this.menuService.menuTree());
    }
}

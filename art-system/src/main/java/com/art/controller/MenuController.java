package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.common.TableRowVO;
import com.art.common.TreeSelectVO;
import com.art.domain.Menu;
import com.art.domain.vo.MenuTreeVO;
import com.art.domain.vo.MenuVO;
import com.art.enums.ApiOperationType;
import com.art.exception.ArtException;
import com.art.service.MenuService;
import com.art.tenant.TenantSupport;
import com.art.utils.SecurityUtil;
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
     * 多租户支持
     */
    private final TenantSupport tenantSupport;

    /**
     * 构造函数
     *
     * @param menuService   菜单服务
     * @param tenantSupport 多租户支持
     */
    public MenuController(MenuService menuService, TenantSupport tenantSupport) {
        this.menuService = menuService;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 根据ID获取菜单信息
     *
     * @param id 菜单ID
     * @return 菜单信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取菜单信息", description = "根据ID获取菜单信息（仅超级管理员）")
    public HttpResult<MenuVO> getById(@PathVariable("id") Long id) {
        this.checkSuperAdmin();
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
    @Operation(summary = "分页查询菜单信息", description = "分页查询菜单信息（仅超级管理员）")
    public HttpResult<Page<MenuVO>> page(Page<MenuVO> page, MenuVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(this.menuService.queryPage(page, vo));
    }

    /**
     * 查询所有菜单信息
     *
     * @return 菜单信息列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询所有菜单信息", description = "查询所有菜单信息（仅超级管理员）")
    public HttpResult<List<Menu>> list() {
        this.checkSuperAdmin();
        return HttpResult.success(this.menuService.selectList());
    }

    /**
     * 新增菜单
     *
     * @param vo 菜单信息
     * @return 新增结果
     */
    @PostMapping("/add")
    @Operation(summary = "新增菜单", description = "新增菜单（仅超级管理员）")
    @ApiLog(module = "菜单管理", operationType = ApiOperationType.INSERT, description = "新增菜单")
    public HttpResult<Boolean> add(@RequestBody MenuVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(menuService.add(vo));
    }

    /**
     * 编辑菜单
     *
     * @param vo 菜单信息
     * @return 编辑结果
     */
    @PutMapping("/edit")
    @Operation(summary = "编辑菜单", description = "编辑菜单（仅超级管理员）")
    @ApiLog(module = "菜单管理", operationType = ApiOperationType.UPDATE, description = "编辑菜单")
    public HttpResult<Boolean> edit(@RequestBody MenuVO vo) {
        this.checkSuperAdmin();
        return HttpResult.success(menuService.edit(vo));
    }

    /**
     * 根据ID删除菜单
     *
     * @param tableRowVO 表格行VO类
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除菜单", description = "根据ID删除菜单（仅超级管理员）")
    @ApiLog(module = "菜单管理", operationType = ApiOperationType.DELETE, description = "删除菜单")
    public HttpResult<Boolean> delete(@RequestBody TableRowVO tableRowVO) {
        this.checkSuperAdmin();
        return HttpResult.success(this.menuService.delete(tableRowVO.getIdList()));
    }

    /**
     * 获取所有菜单树
     *
     * <p>套餐分配/功能权限分配使用：平台级管理菜单已从树中剔除，不可分配给租户。</p>
     *
     * @return 所有菜单树
     */
    @GetMapping("/allMenuTree")
    @Operation(summary = "获取所有菜单树", description = "获取所有菜单树（平台级管理菜单不可分配）")
    public HttpResult<List<TreeSelectVO>> allMenuTree() {
        return HttpResult.success(this.menuService.allMenuTree());
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

    /**
     * 校验当前用户是否为超级管理员<br/>
     * 菜单为系统级共享数据，菜单管理仅允许超级管理员操作
     */
    private void checkSuperAdmin() {
        if (!tenantSupport.isSuperAdmin(SecurityUtil.getUserType())) {
            throw new ArtException("仅超级管理员可以操作菜单管理！");
        }
    }
}

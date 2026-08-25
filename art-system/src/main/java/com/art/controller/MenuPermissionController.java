package com.art.controller;

import com.art.annotation.ApiLog;
import com.art.common.HttpResult;
import com.art.domain.vo.MenuPermissionVO;
import com.art.enums.ApiOperationType;
import com.art.service.MenuPermissionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单权限控制器
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@RestController
@RequestMapping("/menuPermission")
public class MenuPermissionController {

    /**
     * 菜单权限服务
     */
    private final MenuPermissionService menuPermissionService;

    /**
     * 构造方法
     *
     * @param menuPermissionService 菜单权限服务
     */
    public MenuPermissionController(MenuPermissionService menuPermissionService) {
        this.menuPermissionService = menuPermissionService;
    }

    /**
     * 获取菜单权限
     *
     * @return 菜单权限
     */
    @GetMapping
    
    public HttpResult<List<String>> getMenuPermission(@RequestParam("type") String type, @RequestParam("id") Long id) {
        return HttpResult.success(this.menuPermissionService.getMenuPermission(type, id));
    }

    /**
     * 设置菜单权限
     *
     * @param vo 菜单权限信息
     * @return 设置结果
     */
    @PostMapping()
    @ApiLog(module = "菜单权限管理", operationType = ApiOperationType.UPDATE, description = "设置菜单权限")
    public HttpResult<Boolean> setPermission(@RequestBody MenuPermissionVO vo) {
        return HttpResult.success(this.menuPermissionService.setPermission(vo));
    }

}

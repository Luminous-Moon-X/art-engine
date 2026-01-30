package com.art.controller;

import com.art.common.HttpResult;
import com.art.domain.vo.MenuPermissionVO;
import com.art.service.MenuPermissionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public HttpResult<MenuPermissionVO> getMenuPermission() {
        return HttpResult.success(this.menuPermissionService.getMenuPermission());
    }

}

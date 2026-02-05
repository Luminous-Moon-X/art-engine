package com.art;

import cn.dev33.satoken.stp.StpInterface;
import com.art.service.MenuPermissionService;
import com.art.utils.SecurityUtil;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义权限验证接口扩展
 *
 * @author Luminous.X
 * @since 1.1.0
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 菜单权限服务
     */
    private final MenuPermissionService menuPermissionService;


    /**
     * 构造函数
     *
     * @param menuPermissionService 菜单权限服务
     */
    public StpInterfaceImpl(MenuPermissionService menuPermissionService) {
        this.menuPermissionService = menuPermissionService;
    }

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return this.menuPermissionService.getMenuPermission(null, null);
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return SecurityUtil.getRoleCodes();
    }
}

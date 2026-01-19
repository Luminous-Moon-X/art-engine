package com.art.domain.vo;

import lombok.Data;

/**
 * 菜单VO类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class MenuVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 启用标识
     */
    private Boolean enableFlag;
    /**
     * 菜单类型
     */
    private String menuType;
    /**
     * 菜单名称
     */
    private String menuName;
    /**
     * 路由地址
     */
    private String routePath;
    /**
     * 权限标识
     */
    private String permissionSign;
    /**
     * 组件路径
     */
    private String componentPath;
    /**
     * 菜单图标
     */
    private String menuIcon;
    /**
     * 排序
     */
    private Integer orderNum;
    /**
     * 外链
     */
    private String externalLink;
    /**
     * 激活路径
     */
    private String activationPath;
}

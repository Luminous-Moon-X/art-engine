package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单表实体
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("p_sys_menu")
public class Menu extends BaseEntity {
    /**
     * 启用标识
     */
    @Column("enable_flag")
    private Boolean enableFlag;
    /**
     * 菜单类型
     */
    @Column("menu_type")
    private String menuType;
    /**
     * 菜单名称
     */
    @Column("menu_name")
    private String menuName;
    /**
     * 路由地址
     */
    @Column("route_path")
    private String routePath;
    /**
     * 权限标识
     */
    @Column("permission_sign")
    private String permissionSign;
    /**
     * 组件路径
     */
    @Column("component_path")
    private String componentPath;
    /**
     * 菜单图标
     */
    @Column("menu_icon")
    private String menuIcon;
    /**
     * 排序
     */
    @Column("order_num")
    private Integer orderNum;
    /**
     * 外链
     */
    @Column("external_link")
    private String externalLink;
    /**
     * 激活路径
     */
    @Column("activation_path")
    private String activationPath;
}

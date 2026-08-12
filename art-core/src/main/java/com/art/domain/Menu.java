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
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("p_sys_menu")
public class Menu extends BaseEntity {
    /**
     * 启用标识
     */
    @Column("enable_flag")
    private Integer enableFlag;
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
    /**
     * 是否缓存
     */
    @Column("keep_alive")
    private Integer keepAlive;
    /**
     * 是否隐藏
     */
    @Column("hide_flag")
    private Integer hideFlag;
    /**
     * 是否内嵌
     */
    @Column("iframe_flag")
    private Integer iframeFlag;
    /**
     * 是否显示徽标
     */
    @Column("show_badge")
    private Integer showBadge;
    /**
     * 是否固定
     */
    @Column("fixed_tab")
    private Integer fixedTab;
    /**
     * 是否隐藏标签
     */
    @Column("hide_tab")
    private Integer hideTab;
    /**
     * 是否全屏
     */
    @Column("full_screen")
    private Integer fullScreen;
    /**
     * 父级ID
     */
    @Column("parent_id")
    private Long parentId;
}

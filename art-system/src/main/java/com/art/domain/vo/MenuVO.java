package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.util.List;

/**
 * 菜单VO类
 *
 * @author Luminous.X
 * @since 1.0.0
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
    @Query(type = Query.Type.LIKE)
    private String menuName;
    /**
     * 路由地址
     */
    @Query(type = Query.Type.LIKE)
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
    /**
     * 是否缓存
     */
    private Boolean keepAlive;
    /**
     * 是否隐藏
     */
    private Boolean hideFlag;
    /**
     * 是否内嵌
     */
    private Boolean iframeFlag;
    /**
     * 是否显示徽标
     */
    private Boolean showBadge;
    /**
     * 是否固定
     */
    private Boolean fixedTab;
    /**
     * 是否隐藏标签
     */
    private Boolean hideTab;
    /**
     * 是否全屏
     */
    private Boolean fullScreen;
    /**
     * 父级ID
     */
    private Long parentId;
    /**
     * 子菜单
     */
    private List<MenuVO> children;
}

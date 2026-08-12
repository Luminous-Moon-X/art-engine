package com.art.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 菜单元数据VO对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuMetaVO {
    /**
     * 菜单图标
     */
    private String icon;
    /**
     * 菜单名称
     */
    private String title;
    /**
     * 是否缓存
     */
    private Integer keepAlive;
    /**
     * 是否固定
     */
    private Integer fixedTab;
    /**
     * 是否显示徽标
     */
    private Integer showBadge;
    /**
     * 是否隐藏
     */
    private Integer isHide;
    /**
     * 是否隐藏tab
     */
    private Integer isHideTab;
    /**
     * 链接
     */
    private String link;
    /**
     * 是否内嵌
     */
    private Integer isIframe;
    /**
     * 操作权限
     */
    private List<MenuOperationPermissionVO> authList;
    /**
     * 是否全屏
     */
    private Integer isFullScreen;
    /**
     * 激活路径
     */
    private String activePath;
    /**
     * 权限标识
     */
    private String permissionSign;
}

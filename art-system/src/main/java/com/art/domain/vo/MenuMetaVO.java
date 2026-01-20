package com.art.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 菜单元数据VO对象
 */
@Data
@AllArgsConstructor
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
    private Boolean keepAlive;
    /**
     * 是否固定
     */
    private Boolean fixedTab;
    /**
     * 是否显示徽标
     */
    private Boolean showBadge;
    /**
     * 显示的徽标文本
     */
    private String showTextBadge;
    /**
     * 是否隐藏
     */
    private Boolean isHide;
    /**
     * 是否隐藏tab
     */
    private Boolean isHideTab;
    /**
     * 链接
     */
    private String link;
    /**
     * 是否内嵌
     */
    private Boolean isIframe;
    /**
     * 操作权限
     */
    private MenuOperationPermissionVO authList;
    /**
     * 是否全屏
     */
    private Boolean isFullScreen;
    /**
     * 激活路径
     */
    private String activePath;
}

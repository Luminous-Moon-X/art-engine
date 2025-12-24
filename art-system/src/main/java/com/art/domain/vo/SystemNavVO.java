package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.util.List;

/**
 * 系统导航VO类
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
public class SystemNavVO {
    /**
     * 导航ID
     */
    @JsonProperty("id")
    private String id;
    /**
     * 导航名称
     */
    @JsonProperty("navName")
    private String navName;
    /**
     * 导航路径
     */
    @JsonProperty("navPath")
    private String navPath;
    /**
     * 导航图标
     */
    @JsonProperty("navIcon")
    private String navIcon;
    /**
     * 是否平台导航
     */
    @JsonProperty("platformFlag")
    private Boolean platformFlag;
    /**
     * 是否启用
     */
    @JsonProperty("enabledFlag")
    private Boolean enabledFlag;
    /**
     * 父级导航ID
     */
    @JsonProperty("parentId")
    private String parentId;
    /**
     * 排序号
     */
    @JsonProperty("sortNum")
    private Integer sortNum;
    /**
     * 组件路径
     */
    @JsonProperty("component")
    private String component;
    /**
     * 权限标识
     */
    @JsonProperty("permissionKey")
    private String permissionKey;
    /**
     * 角色权限
     */
    @JsonProperty("rolePermission")
    private String rolePermission;
    /**
     * 外部链接
     */
    @JsonProperty("externalLink")
    private String externalLink;
    /**
     * 是否缓存
     */
    @JsonProperty("cacheFlag")
    private Boolean cacheFlag;
    /**
     * 是否隐藏
     */
    @JsonProperty("hideFlag")
    private Boolean hideFlag;
    /**
     * 是否嵌入
     */
    @JsonProperty("embeddedFlag")
    private Boolean embeddedFlag;
    /**
     * 是否全屏
     */
    @JsonProperty("fullScreenFlag")
    private Boolean fullScreenFlag;
    /**
     * 菜单类型
     */
    @JsonProperty("menuType")
    private String menuType;
    /**
     * 子导航
     */
    @JsonProperty("children")
    private List<SystemNavVO> children;
    /**
     * 是否有子导航
     */
    @JsonProperty("hasChildren")
    private Boolean hasChildren;
}

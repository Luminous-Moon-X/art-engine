package com.art.domain;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导航表实体类
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
@Table("p_sys_nav")
public class SysNav {
    /**
     * 导航表主键
     */
    @Id
    @Column("id")
    private Long id;
    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;
    /**
     * 创建人ID
     */
    @Column("create_uid")
    private Integer createUid;
    /**
     * 创建人名称
     */
    @Column("create_uname")
    private String createUname;
    /**
     * 修改时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;
    /**
     * 修改人ID
     */
    @Column("update_uid")
    private Integer updateUid;
    /**
     * 修改人名称
     */
    @Column("update_uname")
    private String updateUname;
    /**
     * 导航名称
     */
    @Column("nav_name")
    private String navName;
    /**
     * 导航路径
     */
    @Column("nav_path")
    private String navPath;
    /**
     * 导航图标
     */
    @Column("nav_icon")
    private String navIcon;
    /**
     * 父级导航ID
     */
    @Column("parent_id")
    private Integer parentId;
    /**
     * 是否平台导航
     */
    @Column("platform_flag")
    private Boolean platformFlag;
    /**
     * 是否启用
     */
    @Column("enabled_flag")
    private Boolean enabledFlag;
    /**
     * 排序号
     */
    @Column("sort_num")
    private Integer sortNum;
    /**
     * 组件路径
     */
    @Column("component")
    private String component;

    /**
     * 权限标识
     */
    @Column("permission_key")
    private String permissionKey;
    /**
     * 角色权限
     */
    @Column("role_permission")
    private String rolePermission;
    /**
     * 外部链接
     */
    @Column("external_link")
    private String externalLink;
    /**
     * 是否缓存
     */
    @Column("cache_flag")
    private Boolean cacheFlag;
    /**
     * 是否隐藏
     */
    @Column("hide_flag")
    private Boolean hideFlag;
    /**
     * 是否嵌入
     */
    @Column("embedded_flag")
    private Boolean embeddedFlag;
    /**
     * 是否全屏
     */
    @Column("full_screen_flag")
    private Boolean fullScreenFlag;
    /**
     * 菜单类型
     */
    @Column("menu_type")
    private String menuType;

    /**
     * 是否固定
     */
    @Column("fixed_flag")
    private Boolean fixedFlag;
}

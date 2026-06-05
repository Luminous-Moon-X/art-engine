package com.art.log.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 菜单日志实体类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_sys_menu_log")
public class MenuLog extends BaseEntity {
    /**
     * 用户ID
     */
    @Column("user_id")
    private Long userId;
    /**
     * 用户名
     */
    @Column("user_name")
    private String userName;
    /**
     * 用户昵称
     */
    @Column("nick_name")
    private String nickName;
    /**
     * 菜单名称
     */
    @Column("menu_name")
    private String menuName;
    /**
     * 菜单路径
     */
    @Column("menu_path")
    private String menuPath;
    /**
     * 点击时间
     */
    @Column("click_time")
    private LocalDateTime clickTime;
}

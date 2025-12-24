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
}

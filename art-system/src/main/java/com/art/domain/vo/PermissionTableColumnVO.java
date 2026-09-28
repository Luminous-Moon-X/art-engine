package com.art.domain.vo;

import lombok.Data;

/**
 * 数据权限-数据库表字段信息VO<br/>
 * 用于数据权限「权限字段」下拉，展示字段名与字段备注
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Data
public class PermissionTableColumnVO {
    /**
     * 字段名
     */
    private String columnName;
    /**
     * 字段备注
     */
    private String columnComment;
}

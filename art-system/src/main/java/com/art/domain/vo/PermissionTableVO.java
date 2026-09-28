package com.art.domain.vo;

import lombok.Data;

/**
 * 数据权限-数据库表信息VO<br/>
 * 用于数据权限「授权客体」下拉，展示表名与表备注
 *
 * @author Luminous.X
 * @since 2.1.0
 */
@Data
public class PermissionTableVO {
    /**
     * 表名
     */
    private String tableName;
    /**
     * 表备注
     */
    private String tableComment;
}

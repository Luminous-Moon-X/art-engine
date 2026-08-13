package com.art.enums;

import lombok.Getter;

/**
 * API 操作类型枚举
 *
 * @author Luminous.X
 * @since 1.2.0
 */
@Getter
public enum ApiOperationType {
    INSERT("新增", "INSERT"),
    UPDATE("修改", "UPDATE"),
    DELETE("删除", "DELETE"),
    QUERY("查询", "QUERY");
    /**
     * 描述
     */
    private final String description;

    /**
     * 代码
     */
    private final String code;

    /**
     * 枚举构造函数
     *
     * @param description 描述
     * @param code        代码
     */
    ApiOperationType(String description, String code) {
        this.description = description;
        this.code = code;
    }
}

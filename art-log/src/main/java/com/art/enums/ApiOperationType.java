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
    INSERT("新增"),
    UPDATE("修改"),
    DELETE("删除"),
    QUERY("查询");
    /**
     * 描述
     */
    private final String description;
    /**
     * 枚举构造函数
     * @param description 描述
     */
    ApiOperationType(String description) {
        this.description = description;
    }
}

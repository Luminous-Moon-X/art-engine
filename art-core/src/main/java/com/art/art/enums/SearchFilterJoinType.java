package com.art.art.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询连接符枚举类
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Getter
@AllArgsConstructor
public enum SearchFilterJoinType {
    AND("AND", "AND条件连接符"),
    OR("OR", "OR条件连接符");
    /**
     * 类型
     */
    private final String type;
    /**
     * 描述
     */
    private final String description;
}

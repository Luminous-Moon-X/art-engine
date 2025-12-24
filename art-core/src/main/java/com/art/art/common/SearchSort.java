package com.art.art.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 查询排序对象
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
public class SearchSort {
    /**
     * 排序类型
     */
    @JsonProperty("SORT_TYPE")
    private String sortType;
    /**
     * 排序字段
     */
    @JsonProperty("SORT_FIELD")
    private String sortField;
}

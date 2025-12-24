package com.art.art.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 分页查询入参VO对象
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
public class CommonSearchVO {
    /**
     * 页码
     */
    @JsonProperty("PAGE_NUM")
    private long pageNum;
    /**
     * 页面大小
     */
    @JsonProperty("PAGE_SIZE")
    private long pageSize;
    /**
     * 排序规则
     */
    @JsonProperty("SORT_RULES")
    private List<SearchSort> sortRules;
    /**
     * 查询条件
     */
    @JsonProperty("FILTERS")
    private List<DataFilter> filters;

}

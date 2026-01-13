package com.art.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 通用数据传输VO
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
public class CommonDataVO {
    /**
     * 主表数据
     */
    @JsonProperty("data")
    private Object data;
    /**
     * 从表数据
     */
    @JsonProperty("dataList")
    private List<Object> dataList;
}

package com.art.art.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 查询数据过滤对象
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
public class DataFilter {
    /**
     * 字段名
     */
    @JsonProperty("FIELD_NAME")
    private String fieldName;
    /**
     * 值
     */
    @JsonProperty("FIELD_VALUE")
    private String fieldValue;
    /**
     * 查询类型：=、!=、>、<、>=、<=、LIKE、NOT LIKE
     */
    @JsonProperty("OPERATION_TYPE")
    private String operationType;
    /**
     * 连接类型：AND/OR
     */
    @JsonProperty("JOIN_TYPE")
    private String joinType;
}

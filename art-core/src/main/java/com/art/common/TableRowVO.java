package com.art.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 表格行VO
 *
 * @author Luminous.X
 * @since 0.0.1
 */
@Data
public class TableRowVO {
    /**
     * 表格选中主键集合
     */
    @JsonProperty("ids")
    private List<String> idList;
}

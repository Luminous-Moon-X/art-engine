package com.art.common;

import lombok.Data;

/**
 * 下拉列表VO类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class SelectVO {
    /**
     * 标签
     */
    private String label;
    /**
     * 值
     */
    private Object value;
}

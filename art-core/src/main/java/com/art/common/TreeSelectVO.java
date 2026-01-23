package com.art.common;

import lombok.Data;

import java.util.List;

/**
 * 树形下拉框VO类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class TreeSelectVO {
    /**
     * 标签
     */
    private String label;
    /**
     * 值
     */
    private Object value;
    /**
     * 子级
     */
    private List<TreeSelectVO> children;
}

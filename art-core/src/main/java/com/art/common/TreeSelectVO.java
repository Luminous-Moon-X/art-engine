package com.art.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 树形下拉框VO类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
     * 是否禁用
     */
    private Boolean disabled = false;
    /**
     * 子级
     */
    private List<TreeSelectVO> children;
}

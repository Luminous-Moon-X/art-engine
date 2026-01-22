package com.art.domain.vo;

import lombok.Data;

/**
 * 字典项VO 用于获取字典数据
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class DictItemVO {
    /**
     * 字典项名称
     */
    private String dictLabel;
    /**
     * 字典项值
     */
    private String dictValue;
    /**
     * 排序号
     */
    private Integer orderNum;
    /**
     * 样式属性
     */
    private String showStyle;
}

package com.art.domain.vo;

import lombok.Data;

@Data
public class DictValueVO {
    /**
     * 字典值ID
     */
    private Long id;
    /**
     * 字典ID
     */
    private Long dictId;
    /**
     * 字典标签
     */
    private String dictLabel;
    /**
     * 字典值
     */
    private String dictValue;
    /**
     * 显示样式
     */
    private String showStyle;
    /**
     * 排序
     */
    private Integer orderNum;
}

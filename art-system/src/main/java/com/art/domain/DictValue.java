package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Table("p_sys_dict_value")
public class DictValue extends BaseEntity {
    /**
     * 字典ID
     */
    @Column("dict_id")
    private Long dictId;
    /**
     * 字典标签
     */
    @Column("dict_label")
    private String dictLabel;
    /**
     * 字典值
     */
    @Column("dict_value")
    private String dictValue;
    /**
     * 显示样式
     */
    @Column("show_style")
    private String showStyle;
    /**
     * 排序
     */
    @Column("order_num")
    private Integer orderNum;
}

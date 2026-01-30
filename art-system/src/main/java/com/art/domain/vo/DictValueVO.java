package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

/**
 * 字典值表VO
 *
 * @author Luminous.X
 * @since 1.0.0
 */
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
    @Query(type = Query.Type.LIKE)
    private String dictLabel;
    /**
     * 字典值
     */
    @Query(type = Query.Type.LIKE)
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

package com.art.domain.vo;

import lombok.Data;

/**
 * 规则项表VO类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class RuleItemVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 规则编码
     */
    private String ruleCode;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 值类型
     */
    private String ruleValueType;
    /**
     * 值
     */
    private String ruleValue;
    /**
     * 备注
     */
    private String remark;
}

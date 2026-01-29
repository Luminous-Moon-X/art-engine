package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 规则表实体
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_sys_rule")
public class Rule extends BaseEntity {
    /**
     * 启用标识
     */
    @Column("enable_flag")
    private Boolean enableFlag;
    /**
     * 规则编码
     */
    @Column("rule_code")
    private String ruleCode;
    /**
     * 规则名称
     */
    @Column("rule_name")
    private String ruleName;
    /**
     * 规则值
     */
    @Column("rule_value")
    private String ruleValue;
    /**
     * 规则值类型
     */
    @Column("rule_value_type")
    private String ruleValueType;
    /**
     * 备注
     */
    @Column("remark")
    private String remark;
}

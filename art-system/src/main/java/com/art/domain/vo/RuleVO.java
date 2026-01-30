package com.art.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 规则表VO类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class RuleVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 创建人
     */
    private Long createId;
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    /**
     * 修改人
     */
    private Long updateId;
    /**
     * 删除标识
     */
    private Boolean enableFlag;
    /**
     * 规则编码
     */
    private String ruleCode;
    /**
     * 规则名称
     */
    private String ruleName;
    /**
     * 规则值
     */
    private String ruleValue;
    /**
     * 值类型
     */
    private String ruleValueType;
    /**
     * 备注
     */
    private String remark;
}

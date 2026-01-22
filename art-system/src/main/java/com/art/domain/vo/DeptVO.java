package com.art.domain.vo;

import lombok.Data;

/**
 * 部门信息VO类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class DeptVO {
    /**
     * 部门ID
     */
    private Long id;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 排序
     */
    private Integer orderNum;
    /**
     * 负责人
     */
    private String chargePerson;
    /**
     * 负责人电话
     */
    private String chargePersonTel;
    /**
     * 负责人邮箱
     */
    private String chargePersonEmail;
    /**
     * 启用状态
     */
    private Boolean enableFlag;
}

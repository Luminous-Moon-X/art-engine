package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门表实体类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("p_sys_dept")
public class Dept extends BaseEntity {
    /**
     * 部门名称
     */
    @Column("dept_name")
    private String deptName;
    /**
     * 排序
     */
    @Column("order_num")
    private Integer orderNum;
    /**
     * 负责人
     */
    @Column("charge_person")
    private String chargePerson;
    /**
     * 负责人电话
     */
    @Column("charge_person_tel")
    private String chargePersonTel;
    /**
     * 负责人邮箱
     */
    @Column("charge_person_email")
    private String chargePersonEmail;
    /**
     * 启用标识
     */
    @Column("enable_flag")
    private Boolean enableFlag;
    /**
     * 父级部门ID
     */
    @Column("parent_id")
    private Long parentId;
}

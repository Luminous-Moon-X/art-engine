package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 字典表
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("p_sys_dict")
public class Dict extends BaseEntity {
    /**
     * 字典名称
     */
    @Column("dict_name")
    private String dictName;
    /**
     * 字典编码
     */
    @Column("dict_code")
    private String dictCode;
    /**
     * 字典类型
     */
    @Column("dict_type")
    private String dictType;
    /**
     * 启用标识
     */
    @Column("enable_flag")
    private Boolean enableFlag;
    /**
     * 备注
     */
    @Column("remark")
    private String remark;
}

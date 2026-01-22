package com.art.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DictVO {
    /**
     * 字典ID
     */
    private Long id;
    /**
     * 字典名称
     */
    private String dictName;
    /**
     * 字典编码
     */
    private String dictCode;
    /**
     * 字典类型
     */
    private String dictType;
    /**
     * 启用标识
     */
    private Boolean enableFlag;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 备注
     */
    private String remark;
}

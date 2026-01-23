package com.art.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 部门树形下拉框VO类
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
@Data
public class DeptTreeSelectVO {
    /**
     * 部门ID
     */
    @JsonProperty("value")
    private Long id;
    /**
     * 部门名称
     */
    @JsonProperty("label")
    private String deptName;
    /**
     * 子部门列表
     */
    private List<DeptTreeSelectVO> children;
}

package com.art.workflow.domain.vo;

import lombok.Data;

/**
 * 流程定义查询VO类<br/>
 * 流程名称、流程编码为模糊匹配条件，流程类别为精确匹配条件，
 * 由服务层统一转换为 warm-flow 的实体查询条件（该引擎不支持 LIKE 查询）
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class DefinitionQueryVO {
    /**
     * 流程编码（模糊匹配）
     */
    private String flowCode;
    /**
     * 流程名称（模糊匹配）
     */
    private String flowName;
    /**
     * 流程类别
     */
    private String category;
    /**
     * 是否发布（0未发布 1已发布 9已失效）
     */
    private Integer isPublish;
    /**
     * 流程激活状态（0挂起 1激活）
     */
    private Integer activityStatus;
}

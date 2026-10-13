package com.art.workflow.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 流程定义VO类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class DefinitionVO {
    /**
     * 主键
     */
    private Long id;
    /**
     * 流程编码（同一编码下可存在多个版本，需保持唯一）
     */
    private String flowCode;
    /**
     * 流程名称
     */
    private String flowName;
    /**
     * 设计器模型（CLASSICS经典模型 MIMIC仿钉钉模型）
     */
    private String modelValue;
    /**
     * 流程类别
     */
    private String category;
    /**
     * 流程版本（由引擎自动递增生成，新增/编辑时无需传入）
     */
    private String version;
    /**
     * 是否发布（0未发布 1已发布 9已失效）
     */
    private Integer isPublish;
    /**
     * 审批表单是否自定义（Y是 N否）
     */
    private String formCustom;
    /**
     * 审批表单路径
     */
    private String formPath;
    /**
     * 流程激活状态（0挂起 1激活）
     */
    private Integer activityStatus;
    /**
     * 监听器类型
     */
    private String listenerType;
    /**
     * 监听器路径
     */
    private String listenerPath;
    /**
     * 扩展字段，预留给业务系统使用
     */
    private String ext;
    /**
     * 创建人
     */
    private String createBy;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新人
     */
    private String updateBy;
    /**
     * 更新时间
     */
    private Date updateTime;
}

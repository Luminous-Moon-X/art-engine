package com.art.workflow.domain.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程定义下拉选项VO类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class DefinitionOptionVO {
    /**
     * 流程类别集合（用于查询条件与表单下拉）
     */
    private List<String> categoryList = new ArrayList<>();
}

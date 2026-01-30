package com.art.domain.vo;

import lombok.Data;

/**
 * 代码生成器参数
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Data
public class CodeGenVO {
    /**
     * 生成表名称数组
     */
    private String[] tableNames;
    /**
     * 作者名称
     */
    private String authorName;
    /**
     * 生成代码类型数组
     */
    private String[] generateTypes;
    /**
     * 模块名称
     */
    private String moduleName;
    /**
     * 根包
     */
    private String rootPackage;
}

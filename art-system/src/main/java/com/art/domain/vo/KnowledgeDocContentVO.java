package com.art.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档内容VO。
 *
 * <p>用于文档内容的查询与编辑接口，对应文档内容表
 * （已解析文档的大文本内容）。</p>
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Data
public class KnowledgeDocContentVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 关联文档表ID（知识库文档表主键）
     */
    private Long docId;

    /**
     * 文档内容解析后的大文本
     */
    private String content;

    /**
     * 内容解析开始时间
     */
    private LocalDateTime parseStartTime;

    /**
     * 内容解析结束时间
     */
    private LocalDateTime parseEndTime;
}
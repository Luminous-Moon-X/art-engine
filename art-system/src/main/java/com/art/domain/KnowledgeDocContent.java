package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文档内容表实体类
 *
 * <p>记录文档内容解析后的大文本，通过文档表ID与知识库文档表关联，
 * 同时记录内容解析的开始时间与结束时间。</p>
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_ai_knowledge_doc_content")
public class KnowledgeDocContent extends BaseEntity {

    /**
     * 关联文档表ID（p_ai_knowledge_doc.id）
     */
    @Column("doc_id")
    private Long docId;

    /**
     * 文档内容解析后的大文本
     */
    @Column("content")
    private String content;

    /**
     * 内容解析开始时间
     */
    @Column("parse_start_time")
    private LocalDateTime parseStartTime;

    /**
     * 内容解析结束时间
     */
    @Column("parse_end_time")
    private LocalDateTime parseEndTime;
}

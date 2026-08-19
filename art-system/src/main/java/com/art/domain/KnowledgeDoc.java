package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 知识库文档表实体类
 *
 * <p>记录上传到知识库的文档信息，包括文档名称、文档类型、OSS文件表ID、
 * 内容解析状态与向量处理状态等。</p>
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_ai_knowledge_doc")
public class KnowledgeDoc extends BaseEntity {

    /**
     * 状态常量：待处理
     */
    public static final String STATUS_PENDING = "pending";

    /**
     * 状态常量：处理中
     */
    public static final String STATUS_PROCESSING = "processing";

    /**
     * 状态常量：完成
     */
    public static final String STATUS_COMPLETE = "complete";

    /**
     * 状态常量：失败
     */
    public static final String STATUS_ERROR = "error";

    /**
     * 文档名称
     */
    @Column("doc_name")
    private String docName;

    /**
     * 文档类型（文件扩展名）
     */
    @Column("doc_type")
    private String docType;

    /**
     * OSS文件表ID
     */
    @Column("oss_file_id")
    private Long ossFileId;

    /**
     * 所属知识库ID（关联 p_ai_knowledge_base.id）
     */
    @Column("kb_id")
    private Long kbId;

    /**
     * 内容解析状态：pending-待处理 processing-处理中 complete-完成 error-失败
     */
    @Column("parse_status")
    private String parseStatus;

    /**
     * 向量处理状态：pending-待处理 processing-处理中 complete-完成 error-失败
     */
    @Column("vector_status")
    private String vectorStatus;

    /**
     * 上传时间
     */
    @Column("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 上传人（用户ID）
     */
    @Column("upload_id")
    private Long uploadId;
}

package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文档VO。
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Data
public class KnowledgeDocVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 文档名称
     */
    @Query(type = Query.Type.LIKE)
    private String docName;

    /**
     * 文档类型（文件扩展名）
     */
    private String docType;

    /**
     * OSS文件表ID
     */
    private Long ossFileId;

    /**
     * 内容解析状态：pending-待处理 processing-处理中 complete-完成 error-失败
     */
    private String parseStatus;

    /**
     * 向量处理状态：pending-待处理 processing-处理中 complete-完成 error-失败
     */
    private String vectorStatus;

    /**
     * 上传时间
     */
    private LocalDateTime uploadTime;

    /**
     * 上传人（用户ID）
     */
    private Long uploadId;

    /**
     * 上传人名称（非表字段，查询时补充）
     */
    private String uploadName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}

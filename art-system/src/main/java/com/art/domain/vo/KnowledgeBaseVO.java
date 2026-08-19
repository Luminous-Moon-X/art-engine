package com.art.domain.vo;

import com.art.annotation.Query;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库VO。
 *
 * <p>同时用于知识库的分页查询与新增/编辑接口，
 * 其中 kbName 支持模糊查询。</p>
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Data
public class KnowledgeBaseVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 知识库名称
     */
    @Query(type = Query.Type.LIKE)
    private String kbName;

    /**
     * 知识库描述
     */
    private String kbDesc;

    /**
     * 封面OSS文件ID（可为空）
     */
    private Long coverOssFileId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
}

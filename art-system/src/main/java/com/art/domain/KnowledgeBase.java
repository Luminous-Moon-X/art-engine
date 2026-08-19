package com.art.domain;

import com.art.common.BaseEntity;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库实体类。
 *
 * <p>知识库是知识库文档的上层分类，一个知识库下可包含多个文档，
 * 用于对文档进行分组管理。</p>
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("p_ai_knowledge_base")
public class KnowledgeBase extends BaseEntity {

    /**
     * 知识库名称
     */
    @Column("kb_name")
    private String kbName;

    /**
     * 知识库描述
     */
    @Column("kb_desc")
    private String kbDesc;

    /**
     * 封面OSS文件ID（可为空，为空时前端使用默认封面；关联 p_sys_oss_file.id）
     */
    @Column("cover_oss_file_id")
    private Long coverOssFileId;
}

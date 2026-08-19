package com.art.service;

import com.art.domain.KnowledgeBase;
import com.art.domain.vo.KnowledgeBaseVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 知识库服务。
 *
 * @author Luminous.X
 * @since 1.3.1
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {

    /**
     * 分页查询知识库信息。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    Page<KnowledgeBaseVO> queryPage(Page<KnowledgeBaseVO> page, KnowledgeBaseVO vo);

    /**
     * 新增知识库。
     *
     * @param vo 知识库信息
     * @return 新增结果
     */
    Boolean add(KnowledgeBaseVO vo);

    /**
     * 编辑知识库。
     *
     * @param vo 知识库信息
     * @return 编辑结果
     */
    Boolean edit(KnowledgeBaseVO vo);

    /**
     * 删除知识库（仅允许删除空知识库，若存在文档则抛出异常）。
     *
     * @param idList 知识库ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);
}

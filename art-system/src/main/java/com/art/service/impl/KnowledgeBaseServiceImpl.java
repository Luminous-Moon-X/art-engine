package com.art.service.impl;

import com.art.domain.KnowledgeBase;
import com.art.domain.KnowledgeDoc;
import com.art.domain.vo.KnowledgeBaseVO;
import com.art.exception.ArtException;
import com.art.mapper.KnowledgeBaseMapper;
import com.art.service.KnowledgeBaseService;
import com.art.service.KnowledgeDocService;
import com.art.service.OssFileService;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库服务实现类。
 *
 * @author Luminous.X
 * @since 1.3.1
 */
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase>
        implements KnowledgeBaseService {

    /**
     * 知识库文档服务（删除知识库前校验是否存在文档）
     */
    private final KnowledgeDocService knowledgeDocService;

    /**
     * 对象存储文件服务（删除知识库时清理封面文件）
     */
    private final OssFileService ossFileService;

    /**
     * 构造函数。
     *
     * @param knowledgeDocService 知识库文档服务
     * @param ossFileService      对象存储文件服务
     */
    public KnowledgeBaseServiceImpl(KnowledgeDocService knowledgeDocService,
                                    OssFileService ossFileService) {
        this.knowledgeDocService = knowledgeDocService;
        this.ossFileService = ossFileService;
    }

    /**
     * 分页查询知识库信息。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @Override
    public Page<KnowledgeBaseVO> queryPage(Page<KnowledgeBaseVO> page, KnowledgeBaseVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        wrapper.orderBy(KnowledgeBase::getId, false);
        return this.getMapper().paginateAs(page, wrapper, KnowledgeBaseVO.class);
    }

    /**
     * 新增知识库。
     *
     * @param vo 知识库信息
     * @return 新增结果
     */
    @Override
    public Boolean add(KnowledgeBaseVO vo) {
        KnowledgeBase entity = new KnowledgeBase();
        entity.setKbName(vo.getKbName());
        entity.setKbDesc(vo.getKbDesc());
        entity.setCoverOssFileId(vo.getCoverOssFileId());
        boolean saved = this.save(entity);
        if (!saved) {
            throw new ArtException("知识库保存失败");
        }
        return true;
    }

    /**
     * 编辑知识库。
     *
     * @param vo 知识库信息
     * @return 编辑结果
     */
    @Override
    public Boolean edit(KnowledgeBaseVO vo) {
        if (vo.getId() == null) {
            throw new ArtException("知识库ID不能为空");
        }
        KnowledgeBase exist = this.getById(vo.getId());
        if (exist == null) {
            throw new ArtException("知识库不存在");
        }
        // 部分字段更新，审计字段由全局监听器自动填充
        KnowledgeBase update = new KnowledgeBase();
        update.setId(vo.getId());
        update.setKbName(vo.getKbName());
        update.setKbDesc(vo.getKbDesc());
        update.setCoverOssFileId(vo.getCoverOssFileId());
        return this.updateById(update);
    }

    /**
     * 删除知识库（仅允许删除空知识库，若存在文档则抛出异常）。
     *
     * @param idList 知识库ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return false;
        }
        for (Long id : idList) {
            KnowledgeBase base = this.getById(id);
            if (base == null) {
                continue;
            }
            // 校验知识库下是否存在文档，存在则不允许删除
            long docCount = knowledgeDocService.count(
                    QueryWrapper.create().eq(KnowledgeDoc::getKbId, id));
            if (docCount > 0) {
                throw new ArtException("该知识库下存在文档，请先删除文档后再删除知识库");
            }
            // 删除封面文件
            if (base.getCoverOssFileId() != null) {
                ossFileService.delete(List.of(base.getCoverOssFileId()));
            }
            boolean removed = this.removeById(id);
            if (!removed) {
                throw new ArtException("知识库删除失败");
            }
        }
        return true;
    }
}

package com.art.service;

import com.art.domain.Dict;
import com.art.domain.vo.DictVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 字典服务
 *
 * @author Luminous.X
 * @since 0.0.1-SNAPSHOT
 */
public interface DictService extends IService<Dict> {
    /**
     * 根据ID查询字典信息
     *
     * @param id 字典ID
     * @return 字典信息
     */
    Dict selectById(Long id);

    /**
     * 分页查询字典信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 字典信息
     */
    Page<DictVO> queryPage(Page<DictVO> page, DictVO vo);

    /**
     * 查询所有字典信息
     *
     * @return 字典信息
     */
    List<Dict> selectList();

    /**
     * 添加字典信息
     *
     * @param vo 字典信息
     * @return 添加结果
     */
    Boolean add(DictVO vo);

    /**
     * 编辑字典信息
     *
     * @param vo 字典信息
     * @return 编辑结果
     */
    Boolean edit(DictVO vo);

    /**
     * 删除字典信息
     *
     * @param idList 字典ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);
}

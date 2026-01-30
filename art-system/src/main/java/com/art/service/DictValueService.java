package com.art.service;

import com.art.domain.DictValue;
import com.art.domain.vo.DictItemVO;
import com.art.domain.vo.DictValueVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 字典值服务
 *
 * @author Luminous.X
 * @since 1.0.0
 */
public interface DictValueService extends IService<DictValue> {
    /**
     * 根据ID查询字典值信息
     *
     * @param id 字典值ID
     * @return 字典值信息
     */
    DictValue selectById(Long id);

    /**
     * 分页查询字典值信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 字典值信息
     */
    Page<DictValueVO> queryPage(Page<DictValueVO> page, DictValueVO vo);

    /**
     * 查询所有字典值信息
     *
     * @return 字典值信息
     */
    List<DictValue> selectList();

    /**
     * 添加字典值信息
     *
     * @param vo 字典值信息
     * @return 添加结果
     */
    Boolean add(DictValueVO vo);

    /**
     * 编辑字典值信息
     *
     * @param vo 字典值信息
     * @return 编辑结果
     */
    Boolean edit(DictValueVO vo);

    /**
     * 删除字典值信息
     *
     * @param idList 字典值ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 根据字典编码查询字典值信息
     *
     * @param dictCode 字典编码
     * @return 字典值信息
     */
    List<DictItemVO> dictByCode(String dictCode);
}

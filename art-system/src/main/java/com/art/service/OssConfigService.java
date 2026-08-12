package com.art.service;

import com.art.domain.OssConfig;
import com.art.domain.vo.OssConfigVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;

import java.util.List;

/**
 * 对象存储配置服务。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
public interface OssConfigService extends IService<OssConfig> {

    /**
     * 根据ID获取配置详情。
     *
     * @param id 配置ID
     * @return 配置详情
     */
    OssConfigVO getDetail(Long id);

    /**
     * 分页查询配置。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    Page<OssConfigVO> queryPage(Page<OssConfigVO> page, OssConfigVO vo);

    /**
     * 查询全部配置。
     *
     * @return 配置列表
     */
    List<OssConfigVO> selectList();

    /**
     * 新增配置。
     *
     * @param vo 配置信息
     * @return 新增结果
     */
    Boolean add(OssConfigVO vo);

    /**
     * 编辑配置。
     *
     * @param vo 配置信息
     * @return 编辑结果
     */
    Boolean edit(OssConfigVO vo);

    /**
     * 删除配置。
     *
     * @param idList 配置ID列表
     * @return 删除结果
     */
    Boolean delete(List<Long> idList);

    /**
     * 启用配置。
     *
     * @param id 配置ID
     * @return 启用结果
     */
    Boolean enable(Long id);
}

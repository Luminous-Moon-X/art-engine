package com.art.service.impl;

import com.art.cache.DictCache;
import com.art.cache.support.CacheRefreshService;
import com.art.domain.DictValue;
import com.art.domain.vo.DictItemVO;
import com.art.domain.vo.DictValueVO;
import com.art.exception.ArtException;
import com.art.mapper.DictValueMapper;
import com.art.service.DictValueService;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 字典值服务实现类
 *
 * @author Luminous.X
 * @since 1.0.0
 */
@Service
public class DictValueServiceImpl extends ServiceImpl<DictValueMapper, DictValue> implements DictValueService {
    /**
     * 字典缓存
     */
    private final DictCache dictCache;
    /**
     * 缓存刷新服务
     */
    private final CacheRefreshService cacheRefreshService;

    /**
     * 构造函数
     *
     * @param dictCache           字典缓存
     * @param cacheRefreshService 缓存刷新服务
     */
    public DictValueServiceImpl(DictCache dictCache, CacheRefreshService cacheRefreshService) {
        this.dictCache = dictCache;
        this.cacheRefreshService = cacheRefreshService;
    }

    /**
     * 根据ID查询字典值信息
     *
     * @param id 字典值ID
     * @return 字典值信息
     */
    @Override
    public DictValue selectById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页查询字典值信息
     *
     * @param page 分页对象
     * @param vo   查询条件对象
     * @return 字典值信息
     */
    @Override
    public Page<DictValueVO> queryPage(Page<DictValueVO> page, DictValueVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        wrapper.orderBy(DictValue::getOrderNum, true);
        return this.getMapper().paginateAs(page, wrapper, DictValueVO.class);
    }

    /**
     * 查询所有字典值信息
     *
     * @return 字典值信息
     */
    @Override
    public List<DictValue> selectList() {
        return this.list();
    }

    /**
     * 添加字典值信息
     *
     * @param vo 字典值信息
     * @return 添加结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(DictValueVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        DictValue entity = ConvertUtil.convert(vo, DictValue.class);
        boolean save = this.save(entity);
        if (save) {
            cacheRefreshService.refreshAfterCommit(dictCache);
        }
        return save;
    }

    /**
     * 编辑字典值信息
     *
     * @param vo 字典值信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(DictValueVO vo) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        DictValue entity = ConvertUtil.convert(vo, DictValue.class);
        boolean b = this.updateById(entity);
        if (b) {
            cacheRefreshService.refreshAfterCommit(dictCache);
        }
        return b;
    }

    /**
     * 删除字典值信息
     *
     * @param idList 字典值ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        boolean b = this.removeByIds(idList);
        if (b) {
            cacheRefreshService.refreshAfterCommit(dictCache);
        }
        return b;
    }

    /**
     * 根据字典编码查询字典值信息
     *
     * @param dictCode 字典编码
     * @return 字典值信息
     */
    @Override
    public List<DictItemVO> dictByCode(String dictCode) {
        return dictCache.getDictByCode(dictCode);
    }
}

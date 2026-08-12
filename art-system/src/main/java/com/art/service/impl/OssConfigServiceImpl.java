package com.art.service.impl;

import cn.hutool.core.util.StrUtil;
import com.art.domain.OssConfig;
import com.art.domain.OssFile;
import com.art.domain.vo.OssConfigVO;
import com.art.exception.ArtException;
import com.art.mapper.OssConfigMapper;
import com.art.mapper.OssFileMapper;
import com.art.service.OssConfigService;
import com.art.storage.ObjectStorageServiceManager;
import com.art.storage.OssSecretCrypto;
import com.art.utils.ConvertUtil;
import com.art.utils.QueryHelper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * 对象存储配置服务实现。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Service
public class OssConfigServiceImpl extends ServiceImpl<OssConfigMapper, OssConfig> implements OssConfigService {

    /**
     * 对象存储服务管理器
     */
    private final ObjectStorageServiceManager objectStorageServiceManager;

    /**
     * Secret Key 加解密工具
     */
    private final OssSecretCrypto ossSecretCrypto;

    /**
     * 文件Mapper
     */
    private final OssFileMapper ossFileMapper;

    /**
     * 构造函数。
     *
     * @param objectStorageServiceManager 对象存储服务管理器
     * @param ossSecretCrypto             Secret Key 加解密工具
     * @param ossFileMapper               文件Mapper
     */
    public OssConfigServiceImpl(ObjectStorageServiceManager objectStorageServiceManager,
                                OssSecretCrypto ossSecretCrypto,
                                OssFileMapper ossFileMapper) {
        this.objectStorageServiceManager = objectStorageServiceManager;
        this.ossSecretCrypto = ossSecretCrypto;
        this.ossFileMapper = ossFileMapper;
    }

    /**
     * 根据ID获取配置详情。
     *
     * @param id 配置ID
     * @return 配置详情
     */
    @Override
    public OssConfigVO getDetail(Long id) {
        OssConfig entity = this.getById(id);
        if (entity == null) {
            throw new ArtException("对象存储配置不存在");
        }
        OssConfigVO vo = ConvertUtil.convert(entity, OssConfigVO.class);
        vo.setSecretKey(null);
        return vo;
    }

    /**
     * 分页查询配置。
     *
     * @param page 分页对象
     * @param vo   查询条件
     * @return 分页数据
     */
    @Override
    public Page<OssConfigVO> queryPage(Page<OssConfigVO> page, OssConfigVO vo) {
        QueryWrapper wrapper = QueryHelper.buildQueryWrapper(vo);
        wrapper.orderBy(OssConfig::getId, false);
        Page<OssConfigVO> result = this.getMapper().paginateAs(page, wrapper, OssConfigVO.class);
        result.getRecords().forEach(item -> item.setSecretKey(null));
        return result;
    }

    /**
     * 查询全部配置。
     *
     * @return 配置列表
     */
    @Override
    public List<OssConfigVO> selectList() {
        QueryWrapper wrapper = QueryWrapper.create().orderBy(OssConfig::getId, false);
        List<OssConfigVO> list = this.listAs(wrapper, OssConfigVO.class);
        list.forEach(item -> item.setSecretKey(null));
        return list;
    }

    /**
     * 新增配置。
     *
     * @param vo 配置信息
     * @return 新增结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean add(OssConfigVO vo) {
        validateConfig(vo, true);
        OssConfig entity = ConvertUtil.convert(vo, OssConfig.class);
        entity.setSecretKey(ossSecretCrypto.encrypt(vo.getSecretKey()));
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(0);
        }
        if (entity.getEnableFlag() == 1) {
            disableOthers(null);
        }
        boolean result = this.save(entity);
        if (result) {
            refreshAfterCommit();
        }
        return result;
    }

    /**
     * 编辑配置。
     *
     * @param vo 配置信息
     * @return 编辑结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean edit(OssConfigVO vo) {
        if (vo == null || vo.getId() == null) {
            throw new ArtException("数据为空，请检查！");
        }
        OssConfig existing = this.getById(vo.getId());
        if (existing == null) {
            throw new ArtException("对象存储配置不存在");
        }
        validateConfig(vo, false);
        OssConfig entity = ConvertUtil.convert(vo, OssConfig.class);
        if (StrUtil.isBlank(vo.getSecretKey())) {
            entity.setSecretKey(existing.getSecretKey());
        } else {
            entity.setSecretKey(ossSecretCrypto.encrypt(vo.getSecretKey()));
        }
        if (entity.getEnableFlag() == null) {
            entity.setEnableFlag(existing.getEnableFlag());
        }
        if (entity.getEnableFlag() == 1) {
            disableOthers(vo.getId());
        }
        boolean result = this.updateById(entity);
        if (result) {
            refreshAfterCommit();
        }
        return result;
    }

    /**
     * 删除配置。
     *
     * @param idList 配置ID列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return false;
        }
        for (Long id : idList) {
            long fileCount = ossFileMapper.selectCountByQuery(
                    QueryWrapper.create().eq(OssFile::getOssConfigId, id)
            );
            if (fileCount > 0) {
                throw new ArtException("该配置下存在上传文件，请先删除文件后再删除配置");
            }
        }
        boolean result = this.removeByIds(idList);
        if (result) {
            refreshAfterCommit();
        }
        return result;
    }

    /**
     * 启用配置。
     *
     * @param id 配置ID
     * @return 启用结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enable(Long id) {
        OssConfig entity = this.getById(id);
        if (entity == null) {
            throw new ArtException("对象存储配置不存在");
        }
        if (entity.getEnableFlag() != null && entity.getEnableFlag() == 1) {
            return true;
        }
        disableOthers(id);
        entity.setEnableFlag(1);
        boolean result = this.updateById(entity);
        if (result) {
            refreshAfterCommit();
        }
        return result;
    }

    /**
     * 校验配置必填项。
     *
     * @param vo     配置信息
     * @param isAdd  是否新增
     */
    private void validateConfig(OssConfigVO vo, boolean isAdd) {
        if (vo == null) {
            throw new ArtException("数据为空，请检查！");
        }
        if (StrUtil.isBlank(vo.getConfigName())) {
            throw new ArtException("配置名称不能为空");
        }
        if (StrUtil.isBlank(vo.getEndpoint())) {
            throw new ArtException("服务地址不能为空");
        }
        if (StrUtil.isBlank(vo.getAccessKey())) {
            throw new ArtException("Access Key不能为空");
        }
        if (isAdd && StrUtil.isBlank(vo.getSecretKey())) {
            throw new ArtException("Secret Key不能为空");
        }
        if (StrUtil.isBlank(vo.getBucketName())) {
            throw new ArtException("Bucket名称不能为空");
        }
    }

    /**
     * 禁用其他启用配置，保证同一时间只有一条启用。
     *
     * @param excludeId 排除的配置ID
     */
    private void disableOthers(Long excludeId) {
        OssConfig update = new OssConfig();
        update.setEnableFlag(0);
        QueryWrapper wrapper = QueryWrapper.create().eq(OssConfig::getEnableFlag, 1);
        if (excludeId != null) {
            wrapper.ne(OssConfig::getId, excludeId);
        }
        this.update(update, wrapper);
    }

    /**
     * 事务提交后重新装配对象存储实例。
     */
    private void refreshAfterCommit() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public int getOrder() {
                    return 0;
                }

                @Override
                public void suspend() {
                }

                @Override
                public void resume() {
                }

                @Override
                public void flush() {
                }

                @Override
                public void savepoint(@NotNull Object savepoint) {
                }

                @Override
                public void savepointRollback(@NotNull Object savepoint) {
                }

                @Override
                public void beforeCommit(boolean readOnly) {
                }

                @Override
                public void beforeCompletion() {
                }

                @Override
                public void afterCommit() {
                    objectStorageServiceManager.refresh();
                }

                @Override
                public void afterCompletion(int status) {
                }
            });
        } else {
            objectStorageServiceManager.refresh();
        }
    }
}

package com.art.storage;

import com.art.context.SecurityContextHolder;
import com.art.domain.OssConfig;
import com.art.exception.ArtException;
import com.art.mapper.OssConfigMapper;
import com.art.tenant.TenantSupport;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对象存储服务管理器。
 *
 * <p>对象存储配置为租户级数据（{@code p_sys_oss_config.tenant_id}），因此管理器
 * 按租户装配并缓存对象存储实例，避免此前全局单实例被"最后刷新的租户"覆盖，
 * 导致其它租户使用到错误租户的桶/密钥。</p>
 *
 * <p>配置解析顺序：当前租户的启用配置 → 全局（无租户）启用配置 →
 * 本租户没有任何配置时回退到任意启用配置（兼容历史单实例部署）。
 * 业务代码只需注入 {@link ObjectStorageService}。</p>
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Slf4j
@Component
public class ObjectStorageServiceManager implements ObjectStorageService {

    /**
     * OSS配置Mapper
     */
    private final OssConfigMapper ossConfigMapper;

    /**
     * 对象存储客户端工厂
     */
    private final ObjectStorageClientFactory objectStorageClientFactory;

    /**
     * 多租户支持（跨租户装配配置需忽略租户条件）
     */
    private final TenantSupport tenantSupport;

    /**
     * 各租户生效的对象存储服务（按需装配，空值表示该租户当前无可用配置）
     */
    private final Map<Long, Optional<ObjectStorageService>> tenantDelegates = new ConcurrentHashMap<>();

    /**
     * 按配置ID装配的对象存储服务（供按文件自身配置读写的场景复用）
     */
    private final Map<Long, ObjectStorageService> configDelegates = new ConcurrentHashMap<>();

    /**
     * 无租户上下文时的兜底对象存储服务
     */
    private volatile ObjectStorageService defaultDelegate;

    /**
     * 构造函数。
     *
     * @param ossConfigMapper            OSS配置Mapper
     * @param objectStorageClientFactory 对象存储客户端工厂
     * @param tenantSupport              多租户支持
     */
    public ObjectStorageServiceManager(OssConfigMapper ossConfigMapper,
                                       ObjectStorageClientFactory objectStorageClientFactory,
                                       TenantSupport tenantSupport) {
        this.ossConfigMapper = ossConfigMapper;
        this.objectStorageClientFactory = objectStorageClientFactory;
        this.tenantSupport = tenantSupport;
    }

    /**
     * 服务启动时自动装配对象存储实例。
     */
    @PostConstruct
    public void init() {
        try {
            refresh();
        } catch (Exception e) {
            log.error("启动时对象存储实例装配失败，可在配置管理后重新装配", e);
        }
    }

    /**
     * 配置变更后重置已装配实例，下次使用时按租户重新装配。
     */
    public synchronized void refresh() {
        List<ObjectStorageService> oldDelegates = new ArrayList<>();
        if (defaultDelegate != null) {
            oldDelegates.add(defaultDelegate);
        }
        tenantDelegates.values().forEach(item -> item.ifPresent(oldDelegates::add));
        oldDelegates.addAll(configDelegates.values());
        tenantDelegates.clear();
        configDelegates.clear();
        defaultDelegate = null;
        oldDelegates.stream().filter(Objects::nonNull).distinct().forEach(this::closeQuietly);
        log.info("对象存储实例已重置，将在下次使用时按租户重新装配");
    }

    /**
     * 按配置ID获取对象存储服务（用于按文件自身配置下载/删除的场景）
     *
     * @param configId OSS配置ID
     * @return 对象存储服务
     */
    public ObjectStorageService serviceForConfig(Long configId) {
        if (configId == null) {
            throw new ArtException("文件对应的对象存储配置不存在");
        }
        ObjectStorageService service = configDelegates.get(configId);
        if (service != null) {
            return service;
        }
        // 配置为跨租户共享，读取需忽略租户条件
        OssConfig config = tenantSupport.systemScope(() -> ossConfigMapper.selectOneById(configId));
        if (config == null) {
            throw new ArtException("文件对应的对象存储配置不存在");
        }
        service = objectStorageClientFactory.create(config);
        configDelegates.put(configId, service);
        return service;
    }

    /**
     * 上传对象。
     *
     * @param objectKey     对象Key
     * @param inputStream   文件流
     * @param contentType   文件类型
     * @param contentLength 文件大小
     * @return 对象Key
     */
    @Override
    public String upload(String objectKey, InputStream inputStream, String contentType, long contentLength) {
        return currentDelegate().upload(objectKey, inputStream, contentType, contentLength);
    }

    /**
     * 下载对象。
     *
     * @param objectKey 对象Key
     * @return 对象内容
     */
    @Override
    public ObjectStorageObject download(String objectKey) {
        return currentDelegate().download(objectKey);
    }

    /**
     * 删除对象。
     *
     * @param objectKey 对象Key
     */
    @Override
    public void delete(String objectKey) {
        currentDelegate().delete(objectKey);
    }

    /**
     * 判断对象是否存在。
     *
     * @param objectKey 对象Key
     * @return 是否存在
     */
    @Override
    public boolean exists(String objectKey) {
        return currentDelegate().exists(objectKey);
    }

    /**
     * 获取对象的访问地址。
     *
     * @param objectKey 对象Key
     * @return 访问地址
     */
    @Override
    public String getUrl(String objectKey) {
        return currentDelegate().getUrl(objectKey);
    }

    /**
     * 获取Bucket名称。
     *
     * @return Bucket名称
     */
    @Override
    public String getBucketName() {
        return currentDelegate().getBucketName();
    }

    /**
     * 获取服务端点。
     *
     * @return 服务端点
     */
    @Override
    public String getEndpoint() {
        return currentDelegate().getEndpoint();
    }

    /**
     * 获取当前生效的OSS配置ID。
     *
     * @return OSS配置ID
     */
    @Override
    public Long getConfigId() {
        return currentDelegate().getConfigId();
    }

    /**
     * 获取当前委托实例：有租户上下文时按租户解析，否则使用全局兜底实例。
     *
     * @return 当前对象存储服务
     */
    private ObjectStorageService currentDelegate() {
        Long tenantId = SecurityContextHolder.getTenantId();
        if (tenantId != null) {
            ObjectStorageService delegate = tenantDelegates
                    .computeIfAbsent(tenantId, id -> Optional.ofNullable(createDelegateForTenant(id)))
                    .orElse(null);
            if (delegate == null) {
                throw new ArtException("未配置启用的对象存储实例，请先启用一条OSS配置");
            }
            return delegate;
        }
        ObjectStorageService current = this.defaultDelegate;
        if (current == null) {
            synchronized (this) {
                current = this.defaultDelegate;
                if (current == null) {
                    current = createDefaultDelegate();
                    this.defaultDelegate = current;
                }
            }
        }
        if (current == null) {
            throw new ArtException("未配置启用的对象存储实例，请先启用一条OSS配置");
        }
        return current;
    }

    /**
     * 按租户装配对象存储实例。
     *
     * @param tenantId 租户ID
     * @return 对象存储服务（无可用配置时返回null）
     */
    private ObjectStorageService createDelegateForTenant(Long tenantId) {
        try {
            // 跨租户读取配置必须忽略租户条件
            List<OssConfig> tenantConfigs = tenantSupport.systemScope(() -> ossConfigMapper.selectListByQuery(
                    QueryWrapper.create()
                            .eq(OssConfig::getTenantId, tenantId)
                            .eq(OssConfig::getDeleteFlag, 0)
                            .orderBy(OssConfig::getId, false)));
            OssConfig config = tenantConfigs.stream()
                    .filter(item -> Integer.valueOf(1).equals(item.getEnableFlag()))
                    .findFirst()
                    .orElse(null);
            if (config == null) {
                // 兼容全局（无租户）配置
                config = selectEnabledConfig(true);
            }
            if (config == null && tenantConfigs.isEmpty()) {
                // 兼容历史单实例部署：本租户没有任何配置时回退到任意启用配置
                config = selectEnabledConfig(false);
            }
            if (config == null) {
                log.warn("租户[{}]未找到启用的对象存储配置", tenantId);
                return null;
            }
            ObjectStorageService delegate = objectStorageClientFactory.create(config);
            log.info("租户[{}]对象存储实例装配成功：{}", tenantId, config.getConfigName());
            return delegate;
        } catch (Exception e) {
            log.error("租户[{}]对象存储实例装配失败", tenantId, e);
            return null;
        }
    }

    /**
     * 装配无租户上下文时使用的兜底实例。
     *
     * @return 对象存储服务（无可用配置时返回null）
     */
    private ObjectStorageService createDefaultDelegate() {
        try {
            OssConfig config = selectEnabledConfig(true);
            if (config == null) {
                config = selectEnabledConfig(false);
            }
            if (config == null) {
                log.warn("未找到启用的对象存储配置，ObjectStorageService 暂未装配");
                return null;
            }
            log.info("对象存储实例装配成功：{}", config.getConfigName());
            return objectStorageClientFactory.create(config);
        } catch (Exception e) {
            log.error("对象存储实例装配失败", e);
            throw new ArtException("对象存储实例装配失败：" + e.getMessage());
        }
    }

    /**
     * 查询一条启用中的对象存储配置（按ID倒序取最新）。
     *
     * @param globalOnly 是否只查询全局（无租户）配置
     * @return 配置（不存在时返回null）
     */
    private OssConfig selectEnabledConfig(boolean globalOnly) {
        return tenantSupport.systemScope(() -> {
            QueryWrapper wrapper = QueryWrapper.create()
                    .eq(OssConfig::getEnableFlag, 1)
                    .eq(OssConfig::getDeleteFlag, 0)
                    .orderBy(OssConfig::getId, false);
            if (globalOnly) {
                wrapper.isNull(OssConfig::getTenantId);
            }
            List<OssConfig> configList = ossConfigMapper.selectListByQuery(wrapper);
            return configList.isEmpty() ? null : configList.getFirst();
        });
    }

    /**
     * 关闭旧的客户端实例。
     *
     * @param oldDelegate 旧实例
     */
    private void closeQuietly(ObjectStorageService oldDelegate) {
        if (oldDelegate instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                log.warn("关闭旧对象存储客户端失败", e);
            }
        }
    }
}

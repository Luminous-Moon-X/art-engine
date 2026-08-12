package com.art.storage;

import com.art.domain.OssConfig;
import com.art.exception.ArtException;
import com.art.mapper.OssConfigMapper;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

/**
 * 对象存储服务管理器。
 *
 * <p>服务启动时读取启用的OSS配置并装配对象存储实例；
 * 配置新增、编辑、启用、删除后会重新装配，业务代码只需注入
 * {@link ObjectStorageService}。</p>
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
     * 当前生效的对象存储服务
     */
    private volatile ObjectStorageService delegate;

    /**
     * 构造函数。
     *
     * @param ossConfigMapper           OSS配置Mapper
     * @param objectStorageClientFactory 对象存储客户端工厂
     */
    public ObjectStorageServiceManager(OssConfigMapper ossConfigMapper,
                                       ObjectStorageClientFactory objectStorageClientFactory) {
        this.ossConfigMapper = ossConfigMapper;
        this.objectStorageClientFactory = objectStorageClientFactory;
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
     * 从数据库重新加载启用的对象存储配置。
     */
    public synchronized void refresh() {
        ObjectStorageService oldDelegate = this.delegate;
        ObjectStorageService newDelegate = null;
        try {
            List<OssConfig> configList = ossConfigMapper.selectListByQuery(QueryWrapper.create()
                    .eq(OssConfig::getEnableFlag, 1)
                    .eq(OssConfig::getDeleteFlag, 0)
                    .orderBy(OssConfig::getId, false));
            if (!configList.isEmpty()) {
                newDelegate = objectStorageClientFactory.create(configList.getFirst());
                log.info("对象存储实例装配成功：{}", configList.getFirst().getConfigName());
            } else {
                log.warn("未找到启用的对象存储配置，ObjectStorageService 暂未装配");
            }
        } catch (Exception e) {
            log.error("对象存储实例装配失败", e);
            throw new ArtException("对象存储实例装配失败：" + e.getMessage());
        }
        this.delegate = newDelegate;
        closeQuietly(oldDelegate);
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
     * 获取当前委托实例。
     *
     * @return 当前对象存储服务
     */
    private ObjectStorageService currentDelegate() {
        ObjectStorageService current = this.delegate;
        if (current == null) {
            throw new ArtException("未配置启用的对象存储实例，请先启用一条OSS配置");
        }
        return current;
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

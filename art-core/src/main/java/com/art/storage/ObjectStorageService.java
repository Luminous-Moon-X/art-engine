package com.art.storage;

import java.io.InputStream;

/**
 * 对象存储服务抽象接口。
 *
 * <p>系统启动时会根据启用的OSS配置自动装配该接口的实现，
 * 业务模块可直接注入本接口完成上传、下载、删除等基本操作。</p>
 *
 * @author Luminous.X
 * @since 1.3.0
 */
public interface ObjectStorageService {

    /**
     * 上传对象。
     *
     * @param objectKey     对象Key
     * @param inputStream   文件流
     * @param contentType   文件类型
     * @param contentLength 文件大小
     * @return 对象Key
     */
    String upload(String objectKey, InputStream inputStream, String contentType, long contentLength);

    /**
     * 下载对象。
     *
     * @param objectKey 对象Key
     * @return 对象内容
     */
    ObjectStorageObject download(String objectKey);

    /**
     * 删除对象。
     *
     * @param objectKey 对象Key
     */
    void delete(String objectKey);

    /**
     * 判断对象是否存在。
     *
     * @param objectKey 对象Key
     * @return 是否存在
     */
    boolean exists(String objectKey);

    /**
     * 获取对象的访问地址。
     *
     * @param objectKey 对象Key
     * @return 访问地址
     */
    String getUrl(String objectKey);

    /**
     * 获取Bucket名称。
     *
     * @return Bucket名称
     */
    String getBucketName();

    /**
     * 获取服务端点。
     *
     * @return 服务端点
     */
    String getEndpoint();

    /**
     * 获取当前生效的OSS配置ID。
     *
     * @return OSS配置ID
     */
    Long getConfigId();
}

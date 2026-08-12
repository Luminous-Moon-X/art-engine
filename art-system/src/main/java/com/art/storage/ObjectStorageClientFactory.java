package com.art.storage;

import com.art.domain.OssConfig;
import com.art.exception.ArtException;
import org.springframework.stereotype.Component;

/**
 * 对象存储客户端工厂。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Component
public class ObjectStorageClientFactory {

    /**
     * Secret Key 加解密工具
     */
    private final OssSecretCrypto ossSecretCrypto;

    /**
     * 构造函数。
     *
     * @param ossSecretCrypto Secret Key 加解密工具
     */
    public ObjectStorageClientFactory(OssSecretCrypto ossSecretCrypto) {
        this.ossSecretCrypto = ossSecretCrypto;
    }

    /**
     * 根据配置创建对象存储服务实例。
     *
     * @param config 对象存储配置
     * @return 对象存储服务
     */
    public ObjectStorageService create(OssConfig config) {
        if (config == null) {
            throw new ArtException("对象存储配置为空，无法创建客户端");
        }
        return new S3ObjectStorageService(config, ossSecretCrypto);
    }
}

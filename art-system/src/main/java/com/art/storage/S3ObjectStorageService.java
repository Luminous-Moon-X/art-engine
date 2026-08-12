package com.art.storage;

import com.art.domain.OssConfig;
import com.art.exception.ArtException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.apache5.Apache5HttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.net.URI;

/**
 * S3兼容对象存储服务实现。
 *
 * <p>支持 AWS S3、MinIO、华为云OBS等S3协议服务。</p>
 *
 * @author Luminous.X
 * @since 1.3.0
 */
public class S3ObjectStorageService implements ObjectStorageService, AutoCloseable {

    /**
     * OSS配置
     */
    private final OssConfig config;

    /**
     * S3客户端
     */
    private final S3Client s3Client;

    /**
     * Secret Key 加解密工具
     */
    private final OssSecretCrypto ossSecretCrypto;

    /**
     * 构造函数。
     *
     * @param config           OSS配置
     * @param ossSecretCrypto  Secret Key 加解密工具
     */
    public S3ObjectStorageService(OssConfig config, OssSecretCrypto ossSecretCrypto) {
        this.config = config;
        this.ossSecretCrypto = ossSecretCrypto;
        this.s3Client = buildClient();
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
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(config.getBucketName())
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(contentLength)
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
            return objectKey;
        } catch (Exception e) {
            throw new ArtException("上传对象存储失败：" + e.getMessage());
        }
    }

    /**
     * 下载对象。
     *
     * @param objectKey 对象Key
     * @return 对象内容
     */
    @Override
    public ObjectStorageObject download(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(config.getBucketName())
                    .key(objectKey)
                    .build();
            var object = s3Client.getObject(request);
            Long contentLength = object.response().contentLength();
            return new ObjectStorageObject(
                    objectKey,
                    config.getBucketName(),
                    object.response().contentType(),
                    contentLength == null ? -1L : contentLength,
                    object
            );
        } catch (Exception e) {
            throw new ArtException("下载对象存储文件失败：" + e.getMessage());
        }
    }

    /**
     * 删除对象。
     *
     * @param objectKey 对象Key
     */
    @Override
    public void delete(String objectKey) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(config.getBucketName())
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw new ArtException("删除对象存储文件失败：" + e.getMessage());
        }
    }

    /**
     * 判断对象是否存在。
     *
     * @param objectKey 对象Key
     * @return 是否存在
     */
    @Override
    public boolean exists(String objectKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(config.getBucketName())
                    .key(objectKey)
                    .build();
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw new ArtException("检查对象存储文件失败：" + e.getMessage());
        }
    }

    /**
     * 获取对象的访问地址。
     *
     * @param objectKey 对象Key
     * @return 访问地址
     */
    @Override
    public String getUrl(String objectKey) {
        String endpoint = config.getEndpoint().replaceAll("/+$", "");
        return endpoint + "/" + config.getBucketName() + "/" + objectKey;
    }

    /**
     * 获取Bucket名称。
     *
     * @return Bucket名称
     */
    @Override
    public String getBucketName() {
        return config.getBucketName();
    }

    /**
     * 获取服务端点。
     *
     * @return 服务端点
     */
    @Override
    public String getEndpoint() {
        return config.getEndpoint();
    }

    /**
     * 获取当前生效的OSS配置ID。
     *
     * @return OSS配置ID
     */
    @Override
    public Long getConfigId() {
        return config.getId();
    }

    /**
     * 关闭S3客户端。
     */
    @Override
    public void close() {
        s3Client.close();
    }

    /**
     * 构建S3客户端。
     *
     * @return S3客户端
     */
    private S3Client buildClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(config.getEndpoint()))
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                config.getAccessKey(),
                                ossSecretCrypto.decrypt(config.getSecretKey())
                        )
                ))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .httpClientBuilder(Apache5HttpClient.builder())
                .build();
    }
}

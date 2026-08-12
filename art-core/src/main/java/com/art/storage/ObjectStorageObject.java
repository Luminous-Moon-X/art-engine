package com.art.storage;

import lombok.Getter;

import java.io.InputStream;

/**
 * 对象存储下载结果。
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Getter
public class ObjectStorageObject {

    /**
     * 对象Key
     */
    private final String objectKey;

    /**
     * Bucket名称
     */
    private final String bucketName;

    /**
     * 文件类型
     */
    private final String contentType;

    /**
     * 文件大小
     */
    private final long contentLength;

    /**
     * 文件流
     */
    private final InputStream inputStream;

    /**
     * 构造函数。
     *
     * @param objectKey     对象Key
     * @param bucketName    Bucket名称
     * @param contentType   文件类型
     * @param contentLength 文件大小
     * @param inputStream   文件流
     */
    public ObjectStorageObject(String objectKey, String bucketName, String contentType,
                               long contentLength, InputStream inputStream) {
        this.objectKey = objectKey;
        this.bucketName = bucketName;
        this.contentType = contentType;
        this.contentLength = contentLength;
        this.inputStream = inputStream;
    }
}

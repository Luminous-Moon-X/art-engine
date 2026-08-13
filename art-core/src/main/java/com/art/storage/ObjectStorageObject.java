package com.art.storage;

import java.io.InputStream;

/**
 * 对象存储下载结果。
 *
 * @param objectKey     对象Key
 * @param bucketName    Bucket名称
 * @param contentType   文件类型
 * @param contentLength 文件大小
 * @param inputStream   文件流
 * @author Luminous.X
 * @since 1.3.0
 */
public record ObjectStorageObject(String objectKey, String bucketName, String contentType, long contentLength,
                                  InputStream inputStream) {

}

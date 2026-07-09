package com.art.properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 平台AI大模型配置
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Getter
@Setter
@NoArgsConstructor
@Component("artModelProperties")
@ConfigurationProperties(prefix = "art.ai-model")
public class BigModelProperties {
    /**
     * 模型名称
     */
    private String model;
    /**
     * 端口地址
     */
    private String endpoint;
    /**
     * 请求路径
     */
    private String requestPath;
    /**
     * 供应商
     */
    private String provider;
    /**
     * apiKey
     */
    private String apiKey;
    /**
     * 是否支持图片
     */
    private Boolean supportImage;
    /**
     * 是否支持音频
     */
    private Boolean supportAudio;
    /**
     * 是否支持视频
     */
    private Boolean supportVideo;
    /**
     * 是否仅支持输入Base64图片
     */
    private Boolean onlyBase64Image;
    /**
     * 是否支持思考
     */
    private Boolean supportThinking;
    /**
     * 是否开启思考
     */
    private Boolean ThinkingEnabled;
}

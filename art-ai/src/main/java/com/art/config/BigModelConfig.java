package com.art.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大模型配置类
 * <p>
 * 绑定 application.yml 中 art.ai-model 前缀的配置项，
 * 用于统一管理 AI 大模型的连接参数和能力开关。
 * </p>
 * 
 * @author Luminous.X
 * @since 1.3.0
 */
@Component
@ConfigurationProperties(prefix = "art.ai-model")
public class BigModelConfig {
    /** API 密钥，用于鉴权 */
    private String apiKey;

    /** 服务端点地址，例如 https://api.openai.com */
    private String endPoint;

    /** 请求路径，例如 /v1/chat/completions */
    private String requestPath;

    /** 模型名称，例如 gpt-4、deepseek-v4-pro */
    private String modelName;

    /** 服务提供商标识，例如 openai、ollama */
    private String provider;

    /** 是否支持图片输入 */
    private Boolean supportImage;

    /** 是否仅支持 base64 格式图片（部分本地模型如 Ollama 仅支持 base64） */
    private Boolean onlyBase64Image;

    /** 是否支持音频输入 */
    private Boolean supportAudio;

    /** 是否支持视频输入 */
    private Boolean supportVideo;

    /** 是否支持思考过程输出 */
    private Boolean supportThinking;

    /** 是否启用思考过程输出 */
    private Boolean enabledThinking;

    public BigModelConfig() {
        
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Boolean getSupportImage() {
        return supportImage;
    }

    public void setSupportImage(Boolean supportImage) {
        this.supportImage = supportImage;
    }

    public Boolean getOnlyBase64Image() {
        return onlyBase64Image;
    }

    public void setOnlyBase64Image(Boolean onlyBase64Image) {
        this.onlyBase64Image = onlyBase64Image;
    }

    public Boolean getSupportAudio() {
        return supportAudio;
    }

    public void setSupportAudio(Boolean supportAudio) {
        this.supportAudio = supportAudio;
    }

    public Boolean getSupportVideo() {
        return supportVideo;
    }

    public void setSupportVideo(Boolean supportVideo) {
        this.supportVideo = supportVideo;
    }

    public void setSupportThinking(Boolean supportThinking) {
        this.supportThinking = supportThinking;
    }

    public Boolean getSupportThinking() {
        return supportThinking;
    }

    public void setEnabledThinking(Boolean enabledThinking) {
        this.enabledThinking = enabledThinking;
    }

    public Boolean getEnabledThinking() {
        return enabledThinking;
    }
}

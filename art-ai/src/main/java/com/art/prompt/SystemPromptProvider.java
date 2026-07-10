package com.art.prompt;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 系统提示词统一提供者
 *
 * <p>负责从 classpath 资源文件中加载系统提示词，并在启动时缓存，
 * 避免每次对话都重复读取文件。</p>
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Getter
@Slf4j
@Component
public class SystemPromptProvider {

    /**
     * 系统提示词资源文件路径
     */
    private static final String SYSTEM_PROMPT_LOCATION = "prompt/system-prompt.txt";

    /**
     * 缓存的系统提示词
     * -- GETTER --
     *  获取系统提示词
     */
    private String systemPrompt;

    /**
     * 启动时加载系统提示词并缓存
     */
    @PostConstruct
    public void init() {
        try (InputStream in = new ClassPathResource(SYSTEM_PROMPT_LOCATION).getInputStream()) {
            this.systemPrompt = StreamUtils.copyToString(in, StandardCharsets.UTF_8).trim();
            log.info("AI big model system prompt loaded successfully!");
        } catch (IOException e) {
            log.error("AI big model system prompt loaded failed:", e);
            this.systemPrompt = "";
        }
    }

}

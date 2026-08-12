package com.art.controller;

import com.art.domain.vo.UserChatVO;
import com.art.service.AIChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * AI 对话控制器
 *
 * @author Luminous.X
 * @since 1.3.0
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AIChatController {

    private final AIChatService aiChatService;


    public AIChatController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    /**
     * 流式对话，以 SSE（Server-Sent Events）方式返回给调用方
     *
     * @param userChatVO 用户对话对象
     * @return SseEmitter 流式响应
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody UserChatVO userChatVO) {
        return this.aiChatService.chat(userChatVO);
    }

    /**
     * 上传解析文档
     *
     * @param file 文档
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@RequestPart("file") MultipartFile file) {
        this.aiChatService.vectorDoc(file);
    }

}

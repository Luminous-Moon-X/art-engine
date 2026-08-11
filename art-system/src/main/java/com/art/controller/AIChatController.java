package com.art.controller;

import com.agentsflex.core.document.Document;
import com.agentsflex.core.store.StoreResult;
import com.agentsflex.store.pgvector.PgvectorVectorStore;
import com.art.DocumentUtil;
import com.art.domain.vo.UserChatVO;
import com.art.service.AIChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

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

    private final PgvectorVectorStore vectorStore;


    public AIChatController(AIChatService aiChatService, PgvectorVectorStore vectorStore) {
        this.aiChatService = aiChatService;
        this.vectorStore = vectorStore;
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
    public void upload(@RequestPart("file") MultipartFile file) throws IOException {
        String documentText = DocumentUtil.extract(file);
        String title = file.getOriginalFilename();
        Document document = Document.of(documentText);
        document.setTitle(title);
        List<Document> documents = DocumentUtil.splitParagraph(document);
        StoreResult store = vectorStore.store(documents);
        System.out.println("exception:" + store.getException());
        System.out.println("message:" + store.getMessage());
    }

}

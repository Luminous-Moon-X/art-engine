package com.art.controller;

import com.art.common.HttpResult;
import com.art.domain.vo.ChatMessageVO;
import com.art.domain.vo.ConversationVO;
import com.art.domain.vo.UserChatVO;
import com.art.service.AIChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
     * 查询当前登录用户的所有对话
     *
     * @return 对话列表（按创建时间倒序）
     */
    @GetMapping("/conversations")
    public HttpResult<List<ConversationVO>> conversations() {
        return HttpResult.success(this.aiChatService.listConversations());
    }

    /**
     * 查询指定对话的全部消息内容
     *
     * @param conversationId 对话ID
     * @return 消息列表（按时间正序）
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public HttpResult<List<ChatMessageVO>> messages(@PathVariable("conversationId") String conversationId) {
        return HttpResult.success(this.aiChatService.listMessages(conversationId));
    }

    /**
     * 重命名对话
     *
     * @param body 请求体，包含 conversationId 与 newName
     * @return 成功提示
     */
    @PutMapping("/conversations/rename")
    public HttpResult<Boolean> renameConversation(@RequestBody java.util.Map<String, String> body) {
        String conversationId = body.get("conversationId");
        String newName = body.get("newName");
        this.aiChatService.renameConversation(conversationId, newName);
        return HttpResult.success(true);
    }

    /**
     * 删除对话及其全部消息
     *
     * @param conversationId 对话ID
     * @return 成功提示
     */
    @DeleteMapping("/conversations/{conversationId}")
    public HttpResult<Boolean> deleteConversation(@PathVariable("conversationId") String conversationId) {
        this.aiChatService.deleteConversation(conversationId);
        return HttpResult.success(true);
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

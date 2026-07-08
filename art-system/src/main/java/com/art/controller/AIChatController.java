package com.art.controller;

import com.agentsflex.core.model.chat.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
public class AIChatController {
    private final ChatModel artChatClient;

    public AIChatController(ChatModel artChatClient) {
        this.artChatClient = artChatClient;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("quest") String quest) {
        return artChatClient.chat(quest);
    }
}

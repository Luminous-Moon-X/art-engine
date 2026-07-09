package com.art.service;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AIChatService {
    SseEmitter chat(String quest);
}

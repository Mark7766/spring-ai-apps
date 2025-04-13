package com.sandy.memory.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/conversations")
    public String createConversation() {
        String conversationId = chatService.createConversation();
        log.info("Create conversation request, returning ID: {}", conversationId);
        return conversationId;
    }
    @PostMapping(value = "/{conversationId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessage(@PathVariable String conversationId, @RequestBody String message) {
        log.info("Send message request, conversationId: {}, message: {}", conversationId, message);
        Flux<String> response = chatService.chat(conversationId, message);
        log.info("Send message response started, conversationId: {}", conversationId);
        return response;
    }

    @GetMapping("/{conversationId}/history")
    public List<ChatMessage> getHistory(@PathVariable String conversationId) {
        log.info("Get history request, conversationId: {}", conversationId);
        List<ChatMessage> history = chatService.getChatHistory(conversationId);
        log.info("Get history response, conversationId: {}, history size: {}", conversationId, history.size());
        return history;
    }

    @GetMapping("/conversations")
    public List<String> getConversations() {
        log.info("Get conversations request");
        List<String> conversations = chatService.getConversations();
        log.info("Get conversations response, count: {}", conversations.size());
        return conversations;
    }
}
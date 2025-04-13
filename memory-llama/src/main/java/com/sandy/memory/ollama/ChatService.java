package com.sandy.memory.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.*;

@Slf4j
@Service
public class ChatService {

    private static final int CHAT_HISTORY_WINDOW_SIZE = 1000;
    private final ChatClient.Builder chatClientBuilder;
    private final StreamingChatModel streamingChatModel;
    private final ChatMemory chatMemory;
    private final Set<String> conversationIdSet = new HashSet<>();

    public ChatService(ChatClient.Builder chatClientBuilder, StreamingChatModel streamingChatModel) {
        this.chatClientBuilder = chatClientBuilder;
        this.streamingChatModel = streamingChatModel;
        this.chatMemory = new InMemoryChatMemory();
    }

    public String createConversation() {
        String conversationId = UUID.randomUUID().toString();
        conversationIdSet.add(conversationId);
        return conversationId;
    }

    public Flux<String> chat(String conversationId, String userMessage) {
        // 创建带有会话ID的ChatClient
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory, conversationId, CHAT_HISTORY_WINDOW_SIZE))
                .build();

        // 保存用户消息并获取上下文
        log.info("Processing user message for conversation {}: {}", conversationId, userMessage);
        // 使用流式调用
        return chatClient.prompt()
                .user(userMessage)
                .stream()
                .content()
                .doOnNext(content -> {
                    // 可选：手动保存模型响应到内存
                    chatMemory.add(conversationId, new org.springframework.ai.chat.messages.AssistantMessage(content));
                })
                .filter(content -> !content.isEmpty())
                .doOnError(e -> log.error("Stream error for conversation {}: {}", conversationId, e.getMessage()));
    }
    public List<ChatMessage> getChatHistory(String conversationId) {
        List<org.springframework.ai.chat.messages.Message> memoryMessages = chatMemory.get(conversationId, CHAT_HISTORY_WINDOW_SIZE);
        List<ChatMessage> history = new ArrayList<>();

        for (org.springframework.ai.chat.messages.Message msg : memoryMessages) {
            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setRole(msg.getMessageType().toString().toLowerCase());
            chatMsg.setContent(msg.getText());
            chatMsg.setTimestamp(System.currentTimeMillis());
            history.add(chatMsg);
        }

        return history;
    }

    public List<String> getConversations() {
        return new ArrayList<>(conversationIdSet);
    }
}
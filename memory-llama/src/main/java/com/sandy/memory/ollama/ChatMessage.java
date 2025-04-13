package com.sandy.memory.ollama;

import lombok.Data;

@Data
public class ChatMessage {
    private String role; // "user" or "assistant"
    private String content;
    private Long timestamp;
}
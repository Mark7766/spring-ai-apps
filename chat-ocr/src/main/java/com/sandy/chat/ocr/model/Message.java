package com.sandy.chat.ocr.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private MessageRole role;
    private String content;
    private List<String> images;
    private LocalDateTime timestamp;
    private MessageStatus status;
}
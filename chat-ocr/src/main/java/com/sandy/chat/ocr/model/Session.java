package com.sandy.chat.ocr.model;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {
    private String id;
    private String name;
    private Scenario scenario;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int messageCount;
    private int imageCount;
}
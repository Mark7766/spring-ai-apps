package com.sandy.chat.ocr.service;

import com.sandy.chat.ocr.model.Message;
import com.sandy.chat.ocr.model.MessageRole;
import com.sandy.chat.ocr.model.MessageStatus;
import com.sandy.chat.ocr.model.Session;
import com.sandy.chat.ocr.util.JsonFileStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 消息管理服务
 */
@Slf4j
@Service
public class MessageService {

    private final SessionService sessionService;
    private final JsonFileStore jsonFileStore;

    @Autowired
    public MessageService(SessionService sessionService, JsonFileStore jsonFileStore) {
        this.sessionService = sessionService;
        this.jsonFileStore = jsonFileStore;
    }

    /**
     * 添加消息
     */
    public Message addMessage(String sessionId, MessageRole role, String content, List<String> images) throws IOException {
        String messageId = "msg-" + UUID.randomUUID().toString();

        Message message = Message.builder()
                .id(messageId)
                .role(role)
                .content(content)
                .images(images)
                .timestamp(LocalDateTime.now())
                .status(MessageStatus.SUCCESS)
                .build();

        // 获取消息文件路径
        Path messagesFile = getMessagesFilePath(sessionId);

        // 追加消息到列表
        jsonFileStore.appendToList(messagesFile, message, Message.class);

        // 更新会话统计
        Session session = sessionService.getSession(sessionId);
        if (session != null) {
            session.setMessageCount(session.getMessageCount() + 1);
            if (images != null && !images.isEmpty()) {
                session.setImageCount(session.getImageCount() + images.size());
            }
            sessionService.updateSession(session);
        }

        log.info("Added message to session {}: {} ({})", sessionId, messageId, role);
        return message;
    }

    /**
     * 获取会话的所有消息
     */
    public List<Message> getMessages(String sessionId) throws IOException {
        Path messagesFile = getMessagesFilePath(sessionId);
        List<Message> messages = jsonFileStore.readList(messagesFile, Message.class);
        return messages != null ? messages : new ArrayList<>();
    }

    /**
     * 获取最新的N条消息
     */
    public List<Message> getLatestMessages(String sessionId, int limit) throws IOException {
        List<Message> allMessages = getMessages(sessionId);
        int size = allMessages.size();
        if (size <= limit) {
            return allMessages;
        }
        return allMessages.subList(size - limit, size);
    }

    /**
     * 获取消息文件路径
     */
    private Path getMessagesFilePath(String sessionId) {
        return sessionService.getSessionDirectory(sessionId).resolve("messages.json");
    }
}


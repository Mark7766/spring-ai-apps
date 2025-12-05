package com.sandy.chat.ocr.service;

import com.sandy.chat.ocr.config.ScenarioProperties;
import com.sandy.chat.ocr.model.MessageRole;
import com.sandy.chat.ocr.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OcrChatService {
    private final ChatModel chatModel;
    private final ScenarioProperties scenarioProperties;
    private final SessionService sessionService;
    private final MessageService messageService;
    private final FileStorageService fileStorageService;
    @Autowired
    public OcrChatService(ChatModel chatModel,
                          ScenarioProperties scenarioProperties,
                          SessionService sessionService,
                          MessageService messageService,
                          FileStorageService fileStorageService) {
        this.chatModel = chatModel;
        this.scenarioProperties = scenarioProperties;
        this.sessionService = sessionService;
        this.messageService = messageService;
        this.fileStorageService = fileStorageService;
    }
    public String processChat(String sessionId, String message, List<MultipartFile> images) throws Exception {
        Session session = sessionService.getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        // Read image bytes first, before any file operations
        List<ImageData> imageDataList = null;
        if (images != null && !images.isEmpty()) {
            imageDataList = new ArrayList<>();
            for (MultipartFile image : images) {
                byte[] bytes = image.getBytes();
                String contentType = image.getContentType();
                if (contentType == null || contentType.isEmpty()) {
                    contentType = "image/png";
                }
                imageDataList.add(new ImageData(bytes, contentType, image.getOriginalFilename()));
            }
        }

        // Save images using the pre-read bytes
        List<String> savedImageNames = null;
        if (imageDataList != null && !imageDataList.isEmpty()) {
            savedImageNames = fileStorageService.saveImages(sessionId, imageDataList);
        }

        messageService.addMessage(sessionId, MessageRole.USER, message, savedImageNames);
        String fullMessage = buildMessageWithScenario(session, message);

        String aiResponse;
        if (imageDataList != null && !imageDataList.isEmpty()) {
            aiResponse = processWithImages(fullMessage, imageDataList);
        } else {
            aiResponse = processTextOnly(fullMessage);
        }

        messageService.addMessage(sessionId, MessageRole.ASSISTANT, aiResponse, null);
        return aiResponse;
    }

    /**
     * Public static class to hold image data that can be accessed by other services
     */
    public static class ImageData {
        public final byte[] bytes;
        public final String contentType;
        public final String originalFilename;

        public ImageData(byte[] bytes, String contentType, String originalFilename) {
            this.bytes = bytes;
            this.contentType = contentType;
            this.originalFilename = originalFilename;
        }
    }
    private String buildMessageWithScenario(Session session, String userMessage) {
        StringBuilder message = new StringBuilder();
        ScenarioProperties.ScenarioConfig config = scenarioProperties.getScenarioConfig(session.getScenario());
        if (config != null && config.getSystemPrompt() != null) {
            message.append(config.getSystemPrompt()).append("\n\n");
        }
        message.append(userMessage);
        return message.toString();
    }
    private String processWithImages(String message, List<ImageData> images) throws Exception {
        log.info("Processing request with {} image(s) using gemma3:12b", images.size());
        List<Media> mediaList = new ArrayList<>();
        for (ImageData imageData : images) {
            log.info("Adding image: {}, type: {}, size: {} bytes",
                    imageData.originalFilename, imageData.contentType, imageData.bytes.length);
            Media media = new Media(
                    MimeTypeUtils.parseMimeType(imageData.contentType),
                    new ByteArrayResource(imageData.bytes)
            );
            mediaList.add(media);
        }

        Map<String, Object> metadata = Map.of("model", "gemma3:12b");
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(mediaList)
                .metadata(metadata)
                .build();
        Prompt prompt = new Prompt(userMessage);
        ChatResponse response = chatModel.call(prompt);
        String aiResponse = response.getResult().getOutput().getText();
        log.info("Received OCR response, length: {} characters", aiResponse.length());
        return aiResponse;
    }
    private String processTextOnly(String message) {
        log.info("Processing text-only request");
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(userMessage);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
    public String processChat(String message, MultipartFile image) throws Exception {
        if (image != null && !image.isEmpty()) {
            // Read image bytes first
            byte[] bytes = image.getBytes();
            String contentType = image.getContentType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "image/png";
            }
            List<ImageData> imageDataList = new ArrayList<>();
            imageDataList.add(new ImageData(bytes, contentType, image.getOriginalFilename()));
            return processWithImages(message, imageDataList);
        } else {
            return processTextOnly(message);
        }
    }
}
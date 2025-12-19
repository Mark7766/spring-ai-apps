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
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.ai.ollama.chat.options.model}")
    private String modelName;

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
        if (imageDataList != null) {
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
     * 使用预处理的图片数据处理聊天请求 (用于 PDF 转换的图片)
     *
     * @param sessionId 会话 ID
     * @param message 用户消息
     * @param imageDataList 图片数据列表
     * @return AI 响应
     */
    public String processChatWithImageData(String sessionId, String message, List<ImageData> imageDataList) throws Exception {
        Session session = sessionService.getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        // 保存图片
        List<String> savedImageNames = null;
        if (imageDataList != null && !imageDataList.isEmpty()) {
            savedImageNames = fileStorageService.saveImages(sessionId, imageDataList);
        }

        // 添加用户消息
        messageService.addMessage(sessionId, MessageRole.USER, message, savedImageNames);

        // 构建完整消息
        String fullMessage = buildMessageWithScenario(session, message);

        // 处理图片
        String aiResponse;
        if (imageDataList != null && !imageDataList.isEmpty()) {
            aiResponse = processWithImages(fullMessage, imageDataList);
        } else {
            aiResponse = processTextOnly(fullMessage);
        }

        // 保存 AI 响应
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
        log.info("Processing request with {} image(s) using model: {}", images.size(), modelName);
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
        // 在消息最前面添加上下文清理提示和图片数量说明
        String contextReset = "【这是一个全新的独立分析请求，请忽略之前的所有对话历史】\n\n";
        String imageCountInfo = String.format("【重要提示：本次请求共上传了%d张图片，请仔细识别实际的图片数量，只分析用户实际上传的图片】\n\n", images.size());

        // 构建增强消息
        String enhancedMessage = contextReset + imageCountInfo + message;
        if (images.size() > 1) {
            enhancedMessage = contextReset + imageCountInfo +
                String.format("【注意：用户实际上传了%d张图片，请逐一查看每张图片，并基于所有图片给出综合分析意见，不要虚构不存在的图片】\n\n%s",
                    images.size(), message);
            log.info("Enhanced message for multi-image analysis: {} images", images.size());
        } else {
            log.info("Enhanced message for single image analysis");
        }

        Map<String, Object> metadata = Map.of("model", modelName);
        UserMessage userMessage = UserMessage.builder()
                .text(enhancedMessage)
                .media(mediaList)
                .metadata(metadata)
                .build();
        Prompt prompt = new Prompt(userMessage);
        ChatResponse response = chatModel.call(prompt);
        String aiResponse = response.getResult().getOutput().getText();
        if (aiResponse != null) {
            log.info("Received AI response for {} image(s), length: {} characters", images.size(), aiResponse.length());
        } else {
            log.warn("Received null AI response for {} image(s)", images.size());
            aiResponse = "抱歉，AI 模型未返回有效响应。";
        }
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
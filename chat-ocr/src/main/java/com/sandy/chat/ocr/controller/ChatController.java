package com.sandy.chat.ocr.controller;

import com.sandy.chat.ocr.dto.ChatResponse;
import com.sandy.chat.ocr.model.Message;
import com.sandy.chat.ocr.service.MessageService;
import com.sandy.chat.ocr.service.OcrChatService;
import com.sandy.chat.ocr.service.PdfProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {

    private final OcrChatService ocrChatService;
    private final MessageService messageService;
    private final PdfProcessingService pdfProcessingService;


    @Autowired
    public ChatController(OcrChatService ocrChatService, MessageService messageService, PdfProcessingService pdfProcessingService) {
        this.ocrChatService = ocrChatService;
        this.messageService = messageService;
        this.pdfProcessingService = pdfProcessingService;
    }

    /**
     * 新版本：带会话的聊天接口，支持图片和 PDF
     */
    @PostMapping(value = "/chat/{sessionId}/message", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatResponse chatWithSession(
            @PathVariable String sessionId,
            @RequestParam("message") String message,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "pdf", required = false) MultipartFile pdf) {

        log.info("Received chat request for session {} - message: {}, images count: {}, pdf: {}",
                sessionId, message, images != null ? images.size() : 0, pdf != null ? pdf.getOriginalFilename() : "none");

        try {
            // 如果有 PDF 文件，转换为图片
            if (pdf != null && !pdf.isEmpty() && pdfProcessingService.isPdfFile(pdf)) {
                log.info("检测到 PDF 文件，开始转换: {}", pdf.getOriginalFilename());
                List<OcrChatService.ImageData> pdfImages = pdfProcessingService.convertPdfToImages(pdf);

                // 使用 PDF 转换的图片处理
                String response = ocrChatService.processChatWithImageData(sessionId, message, pdfImages);
                return new ChatResponse(response, true);
            }

            // 普通图片处理
            String response = ocrChatService.processChat(sessionId, message, images);
            return new ChatResponse(response, true);
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return new ChatResponse("抱歉，处理您的请求时出现错误: " + e.getMessage(), false);
        }
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/chat/{sessionId}/messages")
    public Map<String, Object> getMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "50") int limit) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Message> messages = messageService.getLatestMessages(sessionId, limit);
            response.put("success", true);
            response.put("messages", messages);
        } catch (Exception e) {
            log.error("Error getting messages", e);
            response.put("success", false);
            response.put("message", "获取消息失败: " + e.getMessage());
        }
        return response;
    }

    /**
     * 兼容旧版本的聊天接口
     */
    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatResponse chat(
            @RequestParam("message") String message,
            @RequestParam(value = "image", required = false) MultipartFile image) {

        log.info("Received chat request - message: {}, has image: {}", message, image != null);

        try {
            String response = ocrChatService.processChat(message, image);
            return new ChatResponse(response, true);
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return new ChatResponse("抱歉，处理您的请求时出现错误: " + e.getMessage(), false);
        }
    }
}


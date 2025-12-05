package com.sandy.chat.ocr.service;

import com.sandy.chat.ocr.util.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件存储服务
 */
@Slf4j
@Service
public class FileStorageService {

    private final SessionService sessionService;
    private final ImageUtils imageUtils;

    @Autowired
    public FileStorageService(SessionService sessionService, ImageUtils imageUtils) {
        this.sessionService = sessionService;
        this.imageUtils = imageUtils;
    }

    /**
     * 保存图片文件 - 从预读取的字节数组
     */
    public List<String> saveImages(String sessionId, List<OcrChatService.ImageData> imageDataList) throws IOException {
        List<String> savedFileNames = new ArrayList<>();

        if (imageDataList == null || imageDataList.isEmpty()) {
            return savedFileNames;
        }

        Path imagesDir = sessionService.getSessionDirectory(sessionId).resolve("images");
        Files.createDirectories(imagesDir);

        for (OcrChatService.ImageData imageData : imageDataList) {
            if (!imageUtils.validateImageData(imageData.bytes, imageData.contentType)) {
                log.warn("Invalid image file: {}", imageData.originalFilename);
                continue;
            }

            String fileName = imageUtils.generateFileName(imageData.originalFilename);
            Path targetPath = imagesDir.resolve(fileName);

            imageUtils.saveImageBytes(imageData.bytes, targetPath);
            savedFileNames.add(fileName);

            log.info("Saved image: {} -> {}", imageData.originalFilename, fileName);
        }

        return savedFileNames;
    }

    /**
     * 获取图片路径
     */
    public Path getImagePath(String sessionId, String fileName) {
        return sessionService.getSessionDirectory(sessionId).resolve("images").resolve(fileName);
    }

    /**
     * 删除图片
     */
    public void deleteImage(String sessionId, String fileName) throws IOException {
        Path imagePath = getImagePath(sessionId, fileName);
        Files.deleteIfExists(imagePath);
        log.info("Deleted image: {}", fileName);
    }
}


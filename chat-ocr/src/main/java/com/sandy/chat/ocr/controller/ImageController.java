package com.sandy.chat.ocr.controller;

import com.sandy.chat.ocr.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * 图片资源控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final FileStorageService fileStorageService;

    @Autowired
    public ImageController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    /**
     * 获取图片
     */
    @GetMapping("/{sessionId}/{filename}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String sessionId,
            @PathVariable String filename) {

        try {
            log.info("Requesting image: sessionId={}, filename={}", sessionId, filename);
            Path imagePath = fileStorageService.getImagePath(sessionId, filename);
            log.info("Image path resolved to: {}", imagePath);

            if (!Files.exists(imagePath)) {
                log.warn("Image not found: {}", imagePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(imagePath);

            // 根据文件扩展名确定 Content-Type
            String contentType = Files.probeContentType(imagePath);
            if (contentType == null) {
                // Fallback to extension-based detection
                String fileName = imagePath.getFileName().toString().toLowerCase();
                if (fileName.endsWith(".png")) {
                    contentType = MediaType.IMAGE_PNG_VALUE;
                } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                } else if (fileName.endsWith(".gif")) {
                    contentType = MediaType.IMAGE_GIF_VALUE;
                } else if (fileName.endsWith(".webp")) {
                    contentType = "image/webp";
                } else {
                    contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                }
            }

            log.info("Serving image with content type: {}", contentType);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                    .body(resource);

        } catch (Exception e) {
            log.error("Error serving image", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}


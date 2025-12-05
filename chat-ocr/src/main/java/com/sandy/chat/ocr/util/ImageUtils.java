package com.sandy.chat.ocr.util;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 图片处理工具类
 */
@Slf4j
@Component
public class ImageUtils {

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/png", "image/jpeg", "image/jpg", "image/gif"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * 验证图片文件
     */
    public boolean validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            log.warn("File size exceeds limit: {}", file.getSize());
            return false;
        }

        // 检查MIME类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Invalid content type: {}", contentType);
            return false;
        }

        return true;
    }

    /**
     * 生成唯一的文件名
     */
    public String generateFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + extension;
    }

    /**
     * 获取文件扩展名
     */
    public String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return ".png";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            return filename.substring(lastDot);
        }
        return ".png";
    }

    /**
     * 保存图片文件
     */
    public void saveImage(MultipartFile file, Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent());
        file.transferTo(targetPath.toFile());
        log.info("Image saved to: {}", targetPath);
    }

    /**
     * 保存图片字节数组到文件
     */
    public void saveImageBytes(byte[] imageBytes, Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent());
        Files.write(targetPath, imageBytes);
        log.info("Image saved to: {}", targetPath);
    }

    /**
     * 验证图片数据（字节数组）
     */
    public boolean validateImageData(byte[] imageBytes, String contentType) {
        if (imageBytes == null || imageBytes.length == 0) {
            return false;
        }

        // 检查文件大小
        if (imageBytes.length > MAX_FILE_SIZE) {
            log.warn("Image size exceeds limit: {}", imageBytes.length);
            return false;
        }

        // 检查MIME类型
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Invalid content type: {}", contentType);
            return false;
        }

        return true;
    }

    /**
     * 生成缩略图
     */
    public byte[] generateThumbnail(Path imagePath, int size) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(imagePath.toFile())
                .size(size, size)
                .outputFormat("jpg")
                .outputQuality(0.8)
                .toOutputStream(outputStream);
        return outputStream.toByteArray();
    }

    /**
     * 压缩图片（如果超过指定大小）
     */
    public byte[] compressIfNeeded(byte[] imageData, long maxSize) throws IOException {
        if (imageData.length <= maxSize) {
            return imageData;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(new java.io.ByteArrayInputStream(imageData))
                .scale(0.8)
                .outputQuality(0.7)
                .toOutputStream(outputStream);

        byte[] compressed = outputStream.toByteArray();
        log.info("Compressed image from {} to {} bytes", imageData.length, compressed.length);
        return compressed;
    }
}


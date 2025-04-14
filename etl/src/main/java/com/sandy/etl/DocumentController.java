package com.sandy.etl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {
    @Autowired
    private DocumentService documentService;

    @Autowired
    private RagChatService ragChatService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("收到上传请求: 文件名={}", file.getOriginalFilename());
        try {
            documentService.uploadAndProcessFile(file);
            String message = "文件上传并处理成功";
            log.info("上传响应: 状态=200, 消息={}", message);
            return ResponseEntity.ok(message);
        } catch (IOException e) {
            String errorMessage = "文件处理失败: " + e.getMessage();
            log.error("上传失败: 错误={}", errorMessage, e);
            return ResponseEntity.status(500).body(errorMessage);
        }
    }

    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        log.info("收到删除请求: 文件名={}", fileName);
        try {
            documentService.deleteFile(fileName);
            String message = "文件删除成功";
            log.info("删除响应: 状态=200, 消息={}", message);
            return ResponseEntity.ok(message);
        } catch (RuntimeException e) {
            String errorMessage = "删除文件失败: " + e.getMessage();
            log.error("删除失败: 错误={}", errorMessage, e);
            return ResponseEntity.status(500).body(errorMessage);
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String question) {
        log.info("收到聊天请求: 问题={}", question);
        try {
            String response = ragChatService.chat(question);
            log.info("聊天响应: 状态=200, 回答={}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            String errorMessage = "聊天处理失败: " + e.getMessage();
            log.error("聊天失败: 错误={}", errorMessage, e);
            return ResponseEntity.status(500).body(errorMessage);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        log.info("收到文件列表请求");
        try {
            List<String> files = documentService.listFiles();
            log.info("文件列表响应: 状态=200, 文件数={}", files.size());
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("获取文件列表失败: 错误={}", e.getMessage(), e);
            return ResponseEntity.status(500).body(List.of());
        }
    }

    // 处理文件过大异常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException exc) {
        String errorMessage = "文件过大，请上传小于50MB的文件";
        log.warn("上传文件过大: 错误={}", errorMessage);
        return ResponseEntity.status(400).body(errorMessage);
    }
}
package com.sandy.chat.ocr.util;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
public class JsonFileStore {
    private final ObjectMapper objectMapper;
    @Autowired
    public JsonFileStore(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    public <T> T read(Path path, Class<T> clazz) throws IOException {
        if (!Files.exists(path)) {
            return null;
        }
        T result = objectMapper.readValue(path.toFile(), clazz);
        log.debug("Read object from {}: {}", path, result);
        return result;
    }
    public <T> void write(Path path, T data) throws IOException {
        Files.createDirectories(path.getParent());
        try (FileChannel channel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING)) {
            FileLock lock = channel.lock();
            try {
                String json = objectMapper.writeValueAsString(data);
                channel.write(java.nio.ByteBuffer.wrap(json.getBytes()));
                log.debug("Successfully wrote to file: {}", path);
            } finally {
                lock.release();
            }
        }
    }
    public <T> List<T> readList(Path path, Class<T> clazz) throws IOException {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            // 使用 UTF-8 编码读取文件
            String content = Files.readString(path, StandardCharsets.UTF_8);
            return objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (Exception e) {
            log.error("Error reading list from file {}: {}", path, e.getMessage());
            // 备份损坏的文件
            backupCorruptedFile(path);
            // 返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 备份损坏的文件
     */
    private void backupCorruptedFile(Path path) {
        try {
            Path backupPath = path.getParent().resolve(path.getFileName() + ".corrupted." + System.currentTimeMillis());
            Files.move(path, backupPath);
            log.warn("Corrupted file backed up to: {}", backupPath);
        } catch (IOException e) {
            log.error("Failed to backup corrupted file: {}", path, e);
        }
    }
    public <T> void writeList(Path path, List<T> list) throws IOException {
        write(path, list);
    }
    public <T> void appendToList(Path path, T item, Class<T> clazz) throws IOException {
        List<T> list = readList(path, clazz);
        list.add(item);
        writeList(path, list);
    }
}
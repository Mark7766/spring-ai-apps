package com.sandy.chat.ocr.service;

import com.sandy.chat.ocr.config.ScenarioProperties;
import com.sandy.chat.ocr.model.Scenario;
import com.sandy.chat.ocr.model.Session;
import com.sandy.chat.ocr.util.JsonFileStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 会话管理服务
 */
@Slf4j
@Service
public class SessionService {

    private final ScenarioProperties scenarioProperties;
    private final JsonFileStore jsonFileStore;
    private final Map<String, Session> sessionCache = new HashMap<>();
    private Path storagePath;

    @Autowired
    public SessionService(ScenarioProperties scenarioProperties, JsonFileStore jsonFileStore) {
        this.scenarioProperties = scenarioProperties;
        this.jsonFileStore = jsonFileStore;
    }

    @PostConstruct
    public void init() throws IOException {
        storagePath = Paths.get(scenarioProperties.getStoragePath(), "sessions");
        Files.createDirectories(storagePath);
        loadAllSessions();
        log.info("SessionService initialized. Storage path: {}", storagePath);
    }

    /**
     * 加载所有会话到缓存
     */
    private void loadAllSessions() throws IOException {
        if (!Files.exists(storagePath)) {
            return;
        }

        Files.list(storagePath)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    try {
                        Session session = jsonFileStore.read(path, Session.class);
                        if (session != null) {
                            // 为旧会话设置默认scenario（如果为null）
                            if (session.getScenario() == null) {
                                session.setScenario(Scenario.DESIGN);
                                // 保存更新后的会话
                                jsonFileStore.write(path, session);
                            }
                            sessionCache.put(session.getId(), session);
                        }
                    } catch (IOException e) {
                        log.error("Failed to load session from: {}", path, e);
                    }
                });

        log.info("Loaded {} sessions from disk", sessionCache.size());
    }

    /**
     * 创建新会话
     */
    public Session createSession(String name, Scenario scenario) throws IOException {
        String sessionId = "session-" + UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Session session = Session.builder()
                .id(sessionId)
                .name(name != null ? name : scenario.getDisplayName() + " - " + now.toLocalDate())
                .scenario(scenario)
                .createdAt(now)
                .updatedAt(now)
                .messageCount(0)
                .imageCount(0)
                .build();

        // 保存到磁盘
        Path sessionFile = storagePath.resolve(sessionId + ".json");
        jsonFileStore.write(sessionFile, session);

        // 创建会话目录
        Path sessionDir = getSessionDirectory(sessionId);
        Files.createDirectories(sessionDir);
        Files.createDirectories(sessionDir.resolve("images"));

        // 初始化空消息列表
        Path messagesFile = sessionDir.resolve("messages.json");
        jsonFileStore.writeList(messagesFile, new ArrayList<>());

        // 添加到缓存
        sessionCache.put(sessionId, session);

        log.info("Created new session: {}", sessionId);
        return session;
    }

    /**
     * 获取会话
     */
    public Session getSession(String sessionId) throws IOException {
        Session session = sessionCache.get(sessionId);
        if (session == null) {
            // 尝试从磁盘加载
            Path sessionFile = storagePath.resolve(sessionId + ".json");
            if (Files.exists(sessionFile)) {
                session = jsonFileStore.read(sessionFile, Session.class);
                if (session != null) {
                    // 为旧会话设置默认scenario（如果为null）
                    if (session.getScenario() == null) {
                        session.setScenario(Scenario.DESIGN);
                        // 保存更新后的会话
                        jsonFileStore.write(sessionFile, session);
                    }
                    sessionCache.put(sessionId, session);
                }
            }
        }
        return session;
    }

    /**
     * 获取所有会话列表
     */
    public List<Session> getAllSessions() {
        return sessionCache.values().stream()
                .sorted(Comparator.comparing(Session::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 更新会话
     */
    public void updateSession(Session session) throws IOException {
        session.setUpdatedAt(LocalDateTime.now());
        Path sessionFile = storagePath.resolve(session.getId() + ".json");
        jsonFileStore.write(sessionFile, session);
        sessionCache.put(session.getId(), session);
    }

    /**
     * 删除会话
     */
    public void deleteSession(String sessionId) throws IOException {
        // 从缓存移除
        sessionCache.remove(sessionId);

        // 删除会话文件
        Path sessionFile = storagePath.resolve(sessionId + ".json");
        Files.deleteIfExists(sessionFile);

        // 删除会话目录及所有内容
        Path sessionDir = getSessionDirectory(sessionId);
        if (Files.exists(sessionDir)) {
            deleteDirectoryRecursively(sessionDir);
        }

        log.info("Deleted session: {}", sessionId);
    }

    /**
     * 获取会话目录
     */
    public Path getSessionDirectory(String sessionId) {
        return Paths.get(scenarioProperties.getStoragePath(), "sessions", sessionId);
    }

    /**
     * 递归删除目录
     */
    private void deleteDirectoryRecursively(Path directory) throws IOException {
        if (Files.isDirectory(directory)) {
            Files.list(directory).forEach(path -> {
                try {
                    deleteDirectoryRecursively(path);
                } catch (IOException e) {
                    log.error("Failed to delete: {}", path, e);
                }
            });
        }
        Files.deleteIfExists(directory);
    }
}


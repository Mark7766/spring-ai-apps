package com.sandy.chat.ocr.controller;

import com.sandy.chat.ocr.model.Scenario;
import com.sandy.chat.ocr.model.Session;
import com.sandy.chat.ocr.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    @Autowired
    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    /**
     * 创建新会话
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createSession(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "DESIGN") String scenario) {
        try {
            Scenario scenarioEnum = Scenario.valueOf(scenario.toUpperCase());
            Session session = sessionService.createSession(name, scenarioEnum);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("session", session);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to create session", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "创建会话失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取所有会话列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSessions() {
        try {
            List<Session> sessions = sessionService.getAllSessions();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sessions", sessions);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get sessions", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取会话列表失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取单个会话
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> getSession(@PathVariable String sessionId) {
        try {
            Session session = sessionService.getSession(sessionId);
            if (session == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "会话不存在");
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("session", session);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get session", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "获取会话失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> deleteSession(@PathVariable String sessionId) {
        try {
            sessionService.deleteSession(sessionId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "会话已删除");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to delete session", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "删除会话失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }
}


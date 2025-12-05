package com.sandy.chat.ocr.controller;

import com.sandy.chat.ocr.config.ScenarioProperties;
import com.sandy.chat.ocr.model.Message;
import com.sandy.chat.ocr.model.Scenario;
import com.sandy.chat.ocr.model.Session;
import com.sandy.chat.ocr.service.MessageService;
import com.sandy.chat.ocr.service.SessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
public class WebController {

    private final SessionService sessionService;
    private final MessageService messageService;
    private final ScenarioProperties scenarioProperties;

    @Autowired
    public WebController(SessionService sessionService,
                         MessageService messageService,
                         ScenarioProperties scenarioProperties) {
        this.sessionService = sessionService;
        this.messageService = messageService;
        this.scenarioProperties = scenarioProperties;
    }

    /**
     * 首页 - 显示最新会话或欢迎页
     */
    @GetMapping("/")
    public String index(@RequestParam(required = false) String scenario, Model model) {
        try {
            // 获取所有会话
            List<Session> allSessions = sessionService.getAllSessions();

            // 如果有会话，重定向到最新的会话
            if (!allSessions.isEmpty()) {
                Session latestSession = allSessions.get(0); // 已按更新时间倒序排列
                return "redirect:/chat/" + latestSession.getId();
            }

            // 如果没有会话，显示欢迎页
            Scenario selectedScenario = scenario != null ?
                    Scenario.valueOf(scenario.toUpperCase()) : Scenario.DESIGN;

            model.addAttribute("scenarios", Scenario.values());
            model.addAttribute("selectedScenario", selectedScenario);

            return "welcome";
        } catch (Exception e) {
            log.error("Failed to load index", e);
            return "redirect:/error";
        }
    }

    /**
     * 新建会话
     */
    @GetMapping("/new")
    public String newSession(@RequestParam(defaultValue = "DESIGN") String scenario) {
        try {
            Scenario scenarioEnum = Scenario.valueOf(scenario.toUpperCase());
            Session session = sessionService.createSession(null, scenarioEnum);
            return "redirect:/chat/" + session.getId();
        } catch (Exception e) {
            log.error("Failed to create session", e);
            return "redirect:/";
        }
    }

    /**
     * 会话页面
     */
    @GetMapping("/chat/{sessionId}")
    public String chatPage(@PathVariable String sessionId, Model model) {
        try {
            log.info("Loading chat page for sessionId: {}", sessionId);
            Session session = sessionService.getSession(sessionId);
            if (session == null) {
                log.warn("Session not found: {}", sessionId);
                return "redirect:/";
            }

            log.info("Session loaded: id={}, name={}", session.getId(), session.getName());

            // 获取消息历史
            List<Message> messages = messageService.getMessages(sessionId);
            log.info("Loaded {} messages for session {}", messages.size(), sessionId);

            // 获取所有会话列表
            List<Session> allSessions = sessionService.getAllSessions();

            // 获取场景配置
            ScenarioProperties.ScenarioConfig scenarioConfig =
                    scenarioProperties.getScenarioConfig(session.getScenario());

            // 添加模型属性
            model.addAttribute("session", session);
            model.addAttribute("sessionId", sessionId); // 直接传递 sessionId
            model.addAttribute("messages", messages);
            model.addAttribute("allSessions", allSessions);
            model.addAttribute("scenarios", Scenario.values());
            model.addAttribute("scenarioConfig", scenarioConfig);

            // 验证 session.id 不为 null
            if (session.getId() == null) {
                log.error("WARNING: session.id is null for session object!");
            }

            log.info("Model attributes - sessionId: {}, session.id: {}", sessionId, session.getId());

            return "chat";
        } catch (Exception e) {
            log.error("Error loading chat page", e);
            return "redirect:/";
        }
    }
}


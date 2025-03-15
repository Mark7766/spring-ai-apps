package com.sandy.neo4j.ollama;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AIController {
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private ChatModel chatModel; // 注入聊天模型

    @GetMapping("/ask")
    public String askQuestion(@RequestParam String question) {
        // 提取问题中的关键词（简单示例）
        String companyName = extractCompanyName(question); // 自定义解析逻辑

        // 从Neo4j检索上下文
        Collection<Map<String, Object>> context = employeeService.findEmployeeByCompany(companyName);
        String contextString = formatContext(context);

        // 构造RAG提示
        String prompt = "基于以下信息回答问题:\n" + contextString + "\n问题: " + question;
        System.out.println("prompt:"+prompt.toString());
        ChatResponse chatResponse = chatModel.call(new Prompt(prompt.toString()));
        return chatResponse.getResult().getOutput().getText();
    }

    private String formatContext(Collection<Map<String, Object>> context) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> entry : context) {
            sb.append("员工: ").append(entry.get("employeeName"))
                    .append(", 角色: ").append(entry.get("role"))
                    .append(", 公司: ").append(entry.get("companyName"))
                    .append("\n");
        }
        return sb.toString();
    }

    private String extractCompanyName(String question) {
        // 简单示例，假设问题包含公司名
        if (question.contains("xAI")) return "xAI";
        if (question.contains("Tesla")) return "Tesla";
        return "xAI"; // 默认值
    }
}

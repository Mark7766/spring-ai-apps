package com.sandy.mcp.ollama.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
public class SummarizationController {

    private final ServerToolsProxy serverToolsProxy;
    private final ObjectMapper objectMapper;

    @Autowired
    public SummarizationController(ServerToolsProxy serverToolsProxy, ObjectMapper objectMapper) {
        this.serverToolsProxy = serverToolsProxy;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/summarize")
    public String summarize(@RequestParam("text") String text) throws Exception {
        log.info("text:{}",text);
        // 调用 ServerToolsProxy 获取原始结果
        Object result = serverToolsProxy.summarize(text);

        // 构造 JSON 响应
        Map<String, Object> response = new HashMap<>();
        response.put("summary", result);
        response.put("status", "success");

        // 使用 Jackson 序列化为 JSON 字符串
        return objectMapper.writeValueAsString(response);
    }
}
package com.sandy.mcp.ollama.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
@Component
@Slf4j
public class SummarizationTool {
    @Autowired
    @Lazy
    private  ChatModel chatModel;
    @Tool(
            name = "summarize",
            description = "Summarizes the input text into a concise version"  // 添加描述
    )
    public String summarize(String text) {
        log.info("ask:{}", text);
        ChatResponse response = ChatClient.builder(chatModel)
                .build().prompt()
                .system("You are a helpful assistant.")
                .user("Summarize this text: " + text)
                .call()
                .chatResponse();
        log.info("rsp:{}", JsonParser.toJson(response));
        assert response != null;
        return response.getResult().getOutput().getText();
    }
}

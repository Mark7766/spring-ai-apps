package com.sandy.tools.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.StreamingChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@Slf4j
@RestController
public class AiController {
    @Autowired
    private ChatModel chatModel; // 注入聊天模型
    @Autowired
    private StreamingChatModel streamingChatModel; // 注入聊天模型
    @Autowired
    private DateTimeTools dateTimeTools;


    @PostMapping("/ask")
    public String ask(@RequestBody String userQuery) {
        log.info("askQuery:{}", userQuery);
        String response = ChatClient.create(chatModel)
                .prompt(userQuery)
                .tools(new DateTimeTools())
                .call()
                .content();
        log.info("askQuery:{},response:{}", userQuery, response);
        return response;
    }
}

package com.sandy.tools.ollama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {
    @Autowired
    private ChatModel chatModel; // 注入聊天模型
    @Autowired
    private DateTimeTools dateTimeTools;

    @PostMapping("/ask")
    public String ask(@RequestBody String userQuery) {
        System.out.println("userQuery:"+userQuery);
        String response = ChatClient.create(chatModel)
                .prompt(userQuery)
                .tools(new DateTimeTools())
                .call()
                .content();
        System.out.println("userQuery:"+userQuery+",response:"+response);
        return response;
    }
}

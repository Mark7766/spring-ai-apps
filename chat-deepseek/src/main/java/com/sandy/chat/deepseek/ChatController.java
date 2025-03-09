package com.sandy.chat.deepseek;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final OpenAiChatModel chatModel;

    @Autowired
    public ChatController(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @PostMapping("/chat")
    public MyChatResponse sendMessage(@RequestBody MyChatRequest request) {
        // 这里可以添加你的业务逻辑
        String responseMessage = "服务器收到: " + request.getMessage();
        return new MyChatResponse(this.chatModel.call( request.getMessage()));
    }
}

// 请求和响应的DTO类
class MyChatRequest {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

class MyChatResponse {
    private String message;

    public MyChatResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package com.sandy.chat.azure;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    private ChatModel chatModel;

    @PostMapping("/chat")
    public MyChatResponse sendMessage(@RequestBody MyChatRequest request) {
        System.out.println("request:"+JsonParser.toJson(request));
        // 这里可以添加你的业务逻辑
        MyChatResponse response = new MyChatResponse(this.chatModel.call(request.getMessage()));
        System.out.println("response:"+ JsonParser.toJson(response));
        return response;
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

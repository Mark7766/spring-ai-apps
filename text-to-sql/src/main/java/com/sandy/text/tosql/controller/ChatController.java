package com.sandy.text.tosql.controller;

import com.sandy.text.tosql.request.ChatReq;
import com.sandy.text.tosql.service.DataService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("sandy")
@AllArgsConstructor
public class ChatController {
    @Autowired
    private DataService dataService;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @PostMapping("/chat")
    public ResponseBodyEmitter chat(@RequestBody ChatReq chatReq, HttpServletResponse servletResponse) throws IOException {
        log.info("This a request to chat[trainingReq={}].", JsonParser.toJson(chatReq));
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(5 * 60 * 1000L);
        servletResponse.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        CompletableFuture.runAsync(() -> {
            try {
                dataService.execute(chatReq.getQuestion(),emitter);
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, executorService);
        return emitter;
    }

}

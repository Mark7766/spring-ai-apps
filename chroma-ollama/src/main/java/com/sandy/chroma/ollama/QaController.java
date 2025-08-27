package com.sandy.chroma.ollama;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QaController {

    @Autowired
    private ChatModel chatModel; // 注入聊天模型

    @Autowired
    private ChromaVectorStore vectorStore;

    @GetMapping("/qa")
    public String answerQuestion(@RequestParam String question) {
        System.out.println("q:"+question);
        SearchRequest searchRequest = SearchRequest.builder().query(question).topK(5).build();
        var documents = vectorStore.similaritySearch(searchRequest);

        StringBuilder prompt = new StringBuilder();
        prompt.append("基于以下内容回答问题：\n");
        documents.forEach(doc -> prompt.append(doc.getText()).append("\n"));
        prompt.append("问题：").append(question);
        System.out.println("prompt:"+prompt.toString());
        ChatResponse chatResponse = chatModel.call(new Prompt(prompt.toString()));
        return chatResponse.getResult().getOutput().getText();
    }
    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        System.out.println("ask:"+question);

        ChatResponse response = ChatClient.builder(chatModel)
                .build().prompt()
                .advisors(new QuestionAnswerAdvisor(this.vectorStore))
                .user(question+",用中文回答")
                .call()
                .chatResponse();
        System.out.println("rsp:"+ JsonParser.toJson(response));
        assert response != null;
        return response.getResult().getOutput().getText();
    }
}
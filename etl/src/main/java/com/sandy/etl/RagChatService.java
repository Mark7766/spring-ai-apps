package com.sandy.etl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Slf4j
@Service
public class RagChatService {
    @Autowired
    private ChatModel chatModel;
    @Autowired
    private VectorStore vectorStore;

    public String chat(String question) {
        log.info("question:{}", question);
        SearchRequest searchRequest = SearchRequest.builder().query(question).similarityThreshold(0.1).topK(10).build();
        var documents = vectorStore.similaritySearch(searchRequest);
        StringBuilder prompt = new StringBuilder();
        prompt.append("基于以下内容回答问题：\n");
        assert documents != null;
        documents.forEach(doc -> prompt.append(doc.getText()).append("\n"));
        prompt.append("问题：").append(question);
        log.info("prompt:{}", prompt.toString());
        ChatResponse chatResponse = chatModel.call(new Prompt(prompt.toString()));
        return chatResponse.getResult().getOutput().getText();
    }
}

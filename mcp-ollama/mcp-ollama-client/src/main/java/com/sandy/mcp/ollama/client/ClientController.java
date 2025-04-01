package com.sandy.mcp.ollama.client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ClientController {
    @Autowired
    private ChatModel chatModel;

    @GetMapping("/ask")
    public String ask(@RequestParam("text") String text) throws Exception {
        log.info("ask:{}", text);
        ChatResponse response = ChatClient.builder(chatModel)
                .build().prompt()
                .system("You are a helpful assistant.")
                .user(text)
                .tools(new McpServerProxy())
                .call()
                .chatResponse();
        log.info("rsp:{}", JsonParser.toJson(response));
        assert response != null;
        return response.getResult().getOutput().getText();
    }

    private McpSyncClient mcpSyncClient() {
        HttpClientSseClientTransport transport = new HttpClientSseClientTransport("http://localhost:8091");
        McpSyncClient mcpSyncClient = io.modelcontextprotocol.client.McpClient.sync(transport)
                .build();
        McpSchema.ListToolsResult listToolsResult = mcpSyncClient.listTools();
        log.info(JsonParser.toJson(listToolsResult));
        return mcpSyncClient;
    }
}
package com.sandy.mcp.ollama.client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class SummarizationController {
    @GetMapping("/summarize")
    public String summarize(@RequestParam("text") String text) throws Exception {
        log.info("text:{}", text);
        // 构造工具调用请求
        Map<String, Object> params = new HashMap<>();
        params.put("text", text);
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("summarize", params);
        McpSyncClient client = mcpSyncClient();
        McpSchema.CallToolResult result = client.callTool(request);
        log.info(JsonParser.toJson(result));
        client.close();
        McpSchema.TextContent content = (McpSchema.TextContent) result.content().get(0);
        return content.text();
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
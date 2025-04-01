package com.sandy.mcp.ollama.client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class McpServerProxy {

    @Tool(description = "查询用户所在时区的当前时间")
    String getCurrentDateTime() {
        log.info("getCurrentDateTime:{}",new Date());
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("getCurrentDateTime",new HashMap<>());
        McpSyncClient client = mcpSyncClient();
        McpSchema.CallToolResult result = client.callTool(request);
        log.info(JsonParser.toJson(result));
        client.close();
        McpSchema.TextContent content = (McpSchema.TextContent) result.content().get(0);
        return content.text();
    }

    @Tool(description = "将用户闹钟设置为给定的时间，时间格式为 ISO-8601")
    public void setAlarm(String time) {
        log.info("setAlarm:{}",time);
        // 构造工具调用请求
        Map<String, Object> params = new HashMap<>();
        params.put("time", time);
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("setAlarm",params);
        McpSyncClient client = mcpSyncClient();
        McpSchema.CallToolResult result = client.callTool(request);
        log.info("setAlarm:{}",JsonParser.toJson(result));
        client.close();
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

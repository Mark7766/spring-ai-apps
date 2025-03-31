package com.sandy.mcp.ollama.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.ClientMcpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class ServerToolsProxy {

    private final McpSyncClient mcpSyncClient;

    @Autowired
    public ServerToolsProxy(ClientMcpTransport transport, @Value("${mcp.server.url:http://localhost:8091}") String serverUrl) {
        // 使用同步客户端构建 McpClient
        this.mcpSyncClient = McpClient.sync(transport)
                .requestTimeout(Duration.ofMinutes(3))
                .clientInfo(new McpSchema.Implementation("Spring AI MCP Client", "1.0.0"))
                .build();
    }

    @Tool(name = "summarize")
    public Object summarize(String text) {
        // 构造工具调用请求
        Map<String, Object> params = new HashMap<>();
        params.put("text", text);

        // 使用 McpSyncClient 调用工具
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest("summarize",params);
        // McpSyncClient同步调用
        McpSchema.CallToolResult result = mcpSyncClient.callTool(request);
        // 返回工具调用的结果
        return result;
    }
}

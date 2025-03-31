package com.sandy.mcp.ollama.client;

import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.ClientMcpTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ClientMcpTransport clientMcpTransport(@Value("${mcp.server.url:http://localhost:8091}") String serverUrl) {
        // 使用 HTTP 实现 ClientMcpTransport
        return new HttpClientSseClientTransport(serverUrl);
    }
}
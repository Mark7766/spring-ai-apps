package com.sandy.mcp.ollama.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Slf4j
@Configuration
public class ToolBeanConfig {

    @Bean
    public ToolCallbackProvider toolCallbackProvider(McpServerTool mcpServerTool) throws Exception {
        return MethodToolCallbackProvider.builder().toolObjects(mcpServerTool).build();
    }
}
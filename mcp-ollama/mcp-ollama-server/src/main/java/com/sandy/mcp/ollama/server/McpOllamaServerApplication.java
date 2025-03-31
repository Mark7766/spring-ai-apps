package com.sandy.mcp.ollama.server;

import io.modelcontextprotocol.server.transport.WebMvcSseServerTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpOllamaServerApplication {


	@Autowired
	private WebMvcSseServerTransport sseTransport;

	public static void main(String[] args) {
		SpringApplication.run(McpOllamaServerApplication.class, args);
	}
}
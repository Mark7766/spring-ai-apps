package com.sandy.chroma.ollama;

import org.springframework.ai.vectorstore.chroma.autoconfigure.ChromaVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(exclude = {ChromaVectorStoreAutoConfiguration.class})
public class ChromaOllamaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChromaOllamaApplication.class, args);
	}

}

package com.sandy.chroma.ollama;

import org.springframework.ai.autoconfigure.vectorstore.chroma.ChromaVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication(exclude = {ChromaVectorStoreAutoConfiguration.class})
public class ChromaOllamaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChromaOllamaApplication.class, args);
	}

}

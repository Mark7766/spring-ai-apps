package com.sandy.etl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chroma.vectorstore.ChromaApi;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Slf4j
@Configuration
@Getter
public class ChromaCfg {
    @Value("${spring.ai.vectorstore.chroma.client.host}")
    private String chromaHost;

    @Value("${spring.ai.vectorstore.chroma.client.port}")
    private String chromaPort;

    @Value("${spring.ai.vectorstore.chroma.collection-name}")
    private String collectionName;

    @Value("${spring.ai.vectorstore.chroma.initialize-schema}")
    private boolean initializeSchema;

    @Bean
    public RestClient.Builder builder() {
        return RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory());
    }


    @Bean
    public ChromaApi chromaApi(  RestClient.Builder restClientBuilder) {
        String chromaUrl = chromaHost + ":" + chromaPort;
        return new ChromaApi(chromaUrl, restClientBuilder);
    }
    @Bean
    public ChromaVectorStore chromaVectorStore(EmbeddingModel embeddingModel, ChromaApi chromaApi) {
        return ChromaVectorStore.builder(chromaApi, embeddingModel)
                .collectionName(collectionName)
                .initializeSchema(initializeSchema)
                .build();
    }
}

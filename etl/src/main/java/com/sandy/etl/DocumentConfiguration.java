package com.sandy.etl;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentConfiguration {
    @Bean
    public TokenTextSplitter textSplitter() {
        return new TokenTextSplitter(); // 默认分片配置
    }
}

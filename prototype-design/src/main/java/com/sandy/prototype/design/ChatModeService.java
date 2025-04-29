package com.sandy.prototype.design;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class ChatModeService {

    private static final Integer AZURE_MAX_TOKENS =  12000;
    private static final Integer ALI_MAX_TOKENS =  12000;
    private static final Integer DEEPSEEK_MAX_TOKENS =  8000;
    private static final Double TEMPERATURE = 0.7;
    @Value("${spring.ai.azure.openai.api-key}")
    private String azureOpenaiApiKey;
    @Value("${spring.ai.azure.openai.endpoint}")
    private String azureOpenaiApiEndpoint;
    @Value("${spring.ai.dashscope.api-key}")
    private String alibabaApiKey;
    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;
    @Value("${spring.ai.openai.base-url}")
    private String openaiBaseUrl;
    @Value("${spring.ai.ollama.base-url}")
    private String ollamaBaseUrl;

    public ChatModel getChatModel(PrototypeChatModel prototypeChatModel){
        ChatModel chatModel;
        switch (prototypeChatModel){
            case DEEPSEEK_CHAT ->  chatModel=getDeepSeekChatModel(prototypeChatModel);
            case AZURE_GPT_4O,AZURE_GPT_4O_MINI -> chatModel=getAzureChatModel(prototypeChatModel);
            case OLLAMA_QWEN2_5 -> chatModel=getOllamaChatModel(prototypeChatModel);
            case ALI_QWEN_PLUS,ALI_QWEN_MAX_LATEST ->chatModel=getAliChatModel(prototypeChatModel);
            default -> throw  new RuntimeException("Unknown PrototypeChatModel["+prototypeChatModel+"]");
        }
        return chatModel;
    }

    private ChatModel getOllamaChatModel(PrototypeChatModel prototypeChatModel) {
        return OllamaChatModel.builder()
                .ollamaApi(new OllamaApi(ollamaBaseUrl))
                .defaultOptions(
                        OllamaOptions.builder()
                                .model(prototypeChatModel.getName())
                                .temperature(TEMPERATURE)
                                .build())
                .build();
    }

    private ChatModel getAzureChatModel(PrototypeChatModel prototypeChatModel) {
        var openAIClientBuilder = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(this.azureOpenaiApiKey))
                .endpoint(this.azureOpenaiApiEndpoint);
        var openAIChatOptions = AzureOpenAiChatOptions.builder()
                .deploymentName(prototypeChatModel.getName())
                .temperature(TEMPERATURE)
                .maxTokens(AZURE_MAX_TOKENS)
                .build();

        return AzureOpenAiChatModel.builder()
                .openAIClientBuilder(openAIClientBuilder)
                .defaultOptions(openAIChatOptions)
                .build();
    }

    private ChatModel getAliChatModel(PrototypeChatModel prototypeChatModel) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30000); // 连接超时，30 秒
        requestFactory.setReadTimeout(3*60*1000); // 读取超时，3分钟
        DashScopeApi dashScopeApi = new DashScopeApi("https://dashscope.aliyuncs.com",this.alibabaApiKey,RestClient.builder().requestFactory(requestFactory),WebClient.builder(), RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withModel(prototypeChatModel.getName())
                .withMaxToken(ALI_MAX_TOKENS)
                .withTopP(TEMPERATURE)
                .build();
        return new DashScopeChatModel(dashScopeApi, chatOptions);
    }

    public ChatModel getDeepSeekChatModel(PrototypeChatModel prototypeChatModel) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(30000); // 连接超时，30 秒
        requestFactory.setReadTimeout(3*60*1000); // 读取超时，3分钟
        var openAiApi = OpenAiApi.builder()
                .apiKey(openaiApiKey)
                .baseUrl(openaiBaseUrl)
                .restClientBuilder(RestClient.builder().requestFactory(requestFactory))
                .build();

        // Configure chat options
        var openAiChatOptions = OpenAiChatOptions.builder()
                .model(prototypeChatModel.getName())
                .temperature(TEMPERATURE)
                .maxTokens(DEEPSEEK_MAX_TOKENS)
                .build();

        // Return the OpenAiChatModel with the configured API and options
        return new OpenAiChatModel(
                openAiApi,
                openAiChatOptions
        );
    }
}

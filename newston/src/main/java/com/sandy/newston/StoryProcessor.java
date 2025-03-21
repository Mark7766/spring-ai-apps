package com.sandy.newston;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class StoryProcessor {
    private static final Gson gson = new Gson();
    private ChatClient dashScopeChatClient;
    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";
    private final ChatClient.Builder chatClientBuilder;
    @Value("${sandy.newston.bl-mode}")
    private String model;
    @Value("${spring.ai.dashscope.chat.enabled}")
    private boolean dashscopeEnabled;
    @Value("${spring.ai.openai.chat.enabled}")
    private boolean openaiEnabled;
    @Value("${spring.ai.ollama.chat.enabled}")
    private boolean ollamaEnabled;

    public StoryProcessor(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    @PostConstruct
    public void init() {
        ChatOptions chatOptions =null;
        if(dashscopeEnabled){
            chatOptions =DashScopeChatOptions.builder()
                    .withModel(this.model)
                    .withTopP(0.7)
                    .build();
        }else if(openaiEnabled){
            chatOptions = OpenAiChatOptions.builder()
                    .model("deepseek-chat")
                    .temperature(0.4)
                    .build();
        }else if(ollamaEnabled){
            chatOptions = OllamaOptions.builder()
                    .model(OllamaModel.QWEN_2_5_7B)
                    .temperature(0.4)
                    .build();
        }else{
            throw new RuntimeException("Has no Model");
        }

        log.info("chatOptions is {}.", gson.toJson(chatOptions));
        this.dashScopeChatClient = this.chatClientBuilder
                .defaultSystem(DEFAULT_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())
                )
                .defaultAdvisors(
                        new SimpleLoggerAdvisor()
                )
                .defaultOptions(chatOptions).build();
    }

    public List<String> getTopStories() throws IOException {
        JsonArray topStories = HttpUtil.get("https://hacker-news.firebaseio.com/v0/topstories.json", JsonArray.class);
        List<String> result = new ArrayList<>();
        for (JsonElement el : topStories) {
            result.add(el.getAsString());
        }
        return result;
    }

    public JsonObject getStoryDetails(String storyId) throws IOException {
        return HttpUtil.get("https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json", JsonObject.class);
    }

    public String translateToChinese(String text) throws IOException {
        JsonArray response = HttpUtil.get("https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=zh-CN&dt=t&q="
                + URLEncoder.encode(text, "UTF-8"), JsonArray.class);
        return response.get(0).getAsJsonArray().get(0).getAsJsonArray().get(0).getAsString();
    }

    public String generateSummary(String text) {
        String promptStr = "基于" + text + "生成100字以内的中文摘要，不需要英文摘要和中文描述";
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你一个AI和IT技术专家，也是IT技术博主的助手.'"));
        messages.add(new UserMessage(promptStr));
        Prompt prompt = new Prompt(messages);
        log.info("generateSummary request:{}", gson.toJson(prompt));
        String content = dashScopeChatClient.prompt(prompt).call().content();
        log.info("generateSummary response:{}", gson.toJson(prompt));
        return content;
    }

    public List<JsonObject> filterSoftwareTitles(List<JsonObject> newStoris) {
        Map<String, JsonObject> idStoryMapping = new HashMap<>();
        StringBuffer titles = new StringBuffer();
        newStoris.forEach(story -> {
            String id = story.get("id").getAsString();
            String title = story.get("title").getAsString();
            titles.append(title);
            titles.append(" " + id);
            titles.append("\n");
            idStoryMapping.put(id, story);
        });
        String promptStr = "以下是一些故事的标题。请过滤出与AI或软件开发相关的标题和对应的ID,ID不要用括号括起来,ID后面不用有空格，ID在标题的后边，如标题 ID，回复的内容里只保留标题和ID，如果没有合适的标题，请直接回复没有，故事的标题:\n" + titles.toString();
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你一个AI和IT技术专家，也是IT技术博主的助手.'"));
        messages.add(new UserMessage(promptStr));
        Prompt prompt = new Prompt(messages);
        log.info("filterSoftwareTitles request:{}", gson.toJson(prompt));
        String content = dashScopeChatClient.prompt(prompt).call().content();
        log.info("filterSoftwareTitles response:{}", content);
        String[] lines = content.split("\n");
        List<JsonObject> result = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                String[] parts = line.split(" ");
                if (parts.length > 0) {
                    String id = parts[parts.length - 1];
                    if (idStoryMapping.containsKey(id)) {
                        result.add(idStoryMapping.get(id));
                    } else {
                        log.info("Title[{}] has no story.", id);
                    }
                }
            }
        }
        return result;
    }
}

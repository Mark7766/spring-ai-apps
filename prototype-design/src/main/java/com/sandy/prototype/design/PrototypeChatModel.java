package com.sandy.prototype.design;

import lombok.ToString;
import org.springframework.ai.model.ChatModelDescription;
@ToString
public  enum PrototypeChatModel implements ChatModelDescription {
    DEEPSEEK_CHAT("deepseek-chat"),
    ALI_QWEN_MAX_LATEST("qwen-max-latest"),
    ALI_QWEN_PLUS("qwen-plus"),
    AZURE_GPT_4O("gpt-4o"),
    AZURE_GPT_4O_MINI("gpt-4o-mini"),
    OLLAMA_QWEN2_5("qwen2.5");

    private final String id;

    private PrototypeChatModel(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public String getName() {
        return this.id;
    }

}

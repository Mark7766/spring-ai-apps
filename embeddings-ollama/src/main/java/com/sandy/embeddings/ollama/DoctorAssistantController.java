package com.sandy.embeddings.ollama;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class DoctorAssistantController {

    @Autowired
    private EmbeddingModel embeddingModel; // 注入嵌入模型

    @Autowired
    private ChatModel chatModel; // 注入聊天模型

    // 模拟的医疗知识库
    private final List<String> knowledgeBase = Arrays.asList(
            "肚子疼可能是消化不良引起的，可以尝试喝点温水或吃点清淡的食物。如果持续疼痛，请咨询医生。",
            "腹痛如果伴随发热，可能是感染，建议尽快就医。",
            "背痛可能是肌肉拉伤引起的，可以尝试休息和热敷。"
    );

    // 存储文档的嵌入向量（初始化时计算）
    private final Map<String, float[]> documentEmbeddings = new HashMap<>();

    // 在应用启动时预计算文档的嵌入
    public DoctorAssistantController(EmbeddingModel embeddingModel) {
        for (String doc : knowledgeBase) {
            EmbeddingResponse response = embeddingModel
                    .call(new org.springframework.ai.embedding.EmbeddingRequest(
                            List.of(doc)
                            , OllamaOptions.builder()
                                    .model(OllamaModel.QWEN_2_5_7B)
                                    .build()
                            ));

            documentEmbeddings.put(doc, response.getResult().getOutput());
        }
    }

    @GetMapping("/doctor-assistant")
    public String answerHealthQuestion(@RequestParam String question) {
        // Step 1: 使用 EmbeddingModel 计算问题的嵌入向量
        EmbeddingResponse questionEmbeddingResponse = embeddingModel
                .call(new org.springframework.ai.embedding.EmbeddingRequest(
                        List.of(question)
                        , OllamaOptions.builder()
                        .model(OllamaModel.QWEN_2_5_7B)
                        .build()
                ));
        float[] questionEmbedding = questionEmbeddingResponse.getResult().getOutput();

        // Step 2: 计算问题与知识库中每个文档的相似度，找到最相关的文档
        String mostRelevantDoc = findMostRelevantDocument(questionEmbedding);
        if (mostRelevantDoc == null) {
            return "抱歉，我无法提供针对您问题的具体建议，请咨询专业医生。";
        }

        // Step 3: 使用 ChatModel 根据检索到的文档生成回答
        String promptText = String.format(
                "您是一位医生助手，根据以下信息回答用户的问题：\n知识：%s\n用户问题：%s\n请提供简洁、自然的建议，并提醒用户必要时咨询医生。",
                mostRelevantDoc, question
        );
        ChatResponse chatResponse = chatModel.call(new Prompt(promptText));
        return chatResponse.getResult().getOutput().getText();
    }

    // 计算余弦相似度并找到最相似的文档
    private String findMostRelevantDocument(float[] questionEmbedding) {
        String bestMatch = null;
        double maxSimilarity = -1.0;

        for (Map.Entry<String, float[]> entry : documentEmbeddings.entrySet()) {
            float[] docEmbedding = entry.getValue();
            double similarity = cosineSimilarity(questionEmbedding, docEmbedding);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = entry.getKey();
            }
        }

        // 设置一个相似度阈值，避免不相关的文档
        return maxSimilarity > 0.6 ? bestMatch : null;
    }

    // 计算余弦相似度
    private double cosineSimilarity(float[] vec1, float[] vec2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
package com.sandy.text.tosql.controller;

import com.sandy.text.tosql.model.Training;
import com.sandy.text.tosql.model.TrainingPolicy;
import com.sandy.text.tosql.request.TrainingReq;
import com.sandy.text.tosql.response.ResultRsp;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("sandy")
@AllArgsConstructor
public class TrainingController {
    private static final int TOP_K = 10;
    @Autowired
    private ChatModel chatModel; // 注入聊天模型
    @Autowired
    private final VectorStore vectorStore;


    @DeleteMapping("training/{id}")
    public ResultRsp<Boolean> deleteTraining(@PathVariable String id) {
        log.info("This a request to delete the training[id={}].",id);
        this.vectorStore.delete(List.of(id));
        return ResultRsp.success();
    }

    @GetMapping("training")
    public ResultRsp<List<Document>> describeTraining(@RequestParam String question) {
        log.info("This a request to describe the training[question={}].",question);
        FilterExpressionBuilder expression = new FilterExpressionBuilder();
        List<Document> documents = this.vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(StringUtils.hasText(question) ? question : "ALL DTP`s DDL SQL DOCUMENTATION")
                        .filterExpression(new FilterExpressionBuilder()
                                .in("script_type", TrainingPolicy.DOCUMENTATION.name(), TrainingPolicy.SQL.name(), TrainingPolicy.DDL.name())
                                .build())
                        .topK(TOP_K)
                        .build()
        );
        log.info("This a request to describe the training[question={};documents={}].",question,JsonParser.toJson(documents));
        return ResultRsp.success(documents);
    }

    @PostMapping("training")
    public ResultRsp<Object> createTraining(@RequestBody TrainingReq trainingReq) {
        log.info("This a request to create the training[trainingReq={}].",trainingReq);
        Training training = Training.builder().question(trainingReq.getQuestion()).content(trainingReq.getContent()).policy(TrainingPolicy.valueOf(trainingReq.getPolicy())).build();
        if (training == null) {
            throw new IllegalArgumentException("TrainDao cannot be null");
        }
        try {
            TrainingPolicy policy = training.getPolicy();
            switch (policy.name()) {
                case "DDL":
                    addDDL(training.getContent());
                    break;
                case "SQL":
                    addSQL(training);
                    break;
                case "DOCUMENTATION":
                    addDocumentation(training.getContent());
                    break;
                default:
                    throw new IllegalArgumentException("Invalid train policy: " + policy);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error during training process: " + e);
        }
        return ResultRsp.success();
    }

    private void addDocumentation(String documentation) {
        // 输入验证
        if (documentation == null || documentation.trim().isEmpty()) {
            throw new IllegalArgumentException("Documentation cannot be null or empty");
        }

        try {
            // 创建 Document 对象
            Document doc = new Document(documentation, Map.of("script_type", TrainingPolicy.DOCUMENTATION.name()));

            // 确保线程安全（假设 vectorStore 是多线程环境下的共享资源）
            synchronized (vectorStore) {
                log.info("Adding Documentation Document : {}", doc.getId());
                vectorStore.add(List.of(doc));
            }
        } catch (Exception e) {
            // 异常处理
            // 记录日志并重新抛出异常，以便调用者能够处理
            log.error("Failed to add documentation: " + e.getMessage(), e);
            throw new RuntimeException("Failed to add documentation", e);
        }
    }

    private void addSQL(Training training) {
        // 输入验证
        String question = training.getQuestion();
        String sql = training.getContent();
        if (question == null || question.trim().isEmpty()) {
            log.error("Invalid input: question is null or empty");
            throw new IllegalArgumentException("Question cannot be null or empty");
        }
        if (sql == null || sql.trim().isEmpty()) {
            log.error("Invalid input: sql is null or empty");
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }

        try {
            // 将文档添加到向量存储
            synchronized (vectorStore) {
                Document document = new Document(JsonParser.toJson(training), Map.of("script_type", TrainingPolicy.SQL.name()));
                log.info("Adding QuestionSql Document : {}", document.getId());
                vectorStore.add(List.of(document));
            }
        } catch (Exception e) {
            // 异常处理与日志记录
            log.error("Failed to add question and SQL to vector store", e);
            throw new RuntimeException("Failed to add question and SQL to vector store", e);
        }
    }

    private void addDDL(String ddl) {
        synchronized (vectorStore) {
            Document document = new Document(ddl, Map.of("script_type", TrainingPolicy.DDL.name()));
            log.info("Adding DDL Document : {}", document.getId());
            vectorStore.add(List.of(document));
        }
        log.info("DDL added successfully: {}", ddl);
    }

}

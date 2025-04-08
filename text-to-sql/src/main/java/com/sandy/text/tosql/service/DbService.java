package com.sandy.text.tosql.service;

import com.sandy.text.tosql.config.TextToSqlCfg;
import com.sandy.text.tosql.model.SqlAssistantPrompt;
import com.sandy.text.tosql.model.SqlpromptBuilder;
import com.sandy.text.tosql.model.TrainingPolicy;
import com.sandy.text.tosql.util.SqlExtractorUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Slf4j
public class DbService {
    private static final int TOP_K = 5;
    @Autowired
    private ChatModel chatModel; // 注入聊天模型
    @Autowired
    private TextToSqlCfg textToSqlCfg;
    @Autowired
    private  VectorStore vectorStore;
    @PersistenceContext
    private EntityManager entityManager;

    public String generateSql(String question) {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be null or empty");
        }
        List<Document> questionSqlList = this.searchVectorByTag(question, TrainingPolicy.SQL);
        List<Document> ddlList = this.searchVectorByTag(question, TrainingPolicy.DDL);
        List<Document> documentList = this.searchVectorByTag(question, TrainingPolicy.DOCUMENTATION);
        SqlpromptBuilder sqlprompt = SqlpromptBuilder.builder().question(question).questionSqlList(questionSqlList).ddlList(ddlList).documentList(documentList).build();
        Prompt prompt = SqlAssistantPrompt.getSqlPrompt(sqlprompt);
        log.info("Generating SQL Prompt for first:\n {}", prompt.getContents());
        ChatResponse llmResponse = ChatClient.builder(chatModel)
                .build()
                .prompt(prompt)
                .call()
                .chatResponse();
        log.info("Generating SQL From LLM {}", JsonParser.toJson(llmResponse));
        String rspText= llmResponse.getResult().getOutput().getText();
        if (rspText.contains("intermediate_sql")) {
            String intermediateSql = SqlExtractorUtils.extractSql(rspText);
            List<Map<String, Object>> executed = executeSql(intermediateSql);
            sqlprompt.getDocumentList().add(
                    new Document(String.format("""
                        The following is a pandas DataFrame with the results of the intermediate SQL query %s:\\n%s
                        """,intermediateSql, executed.toString()
                    )));
            prompt = SqlAssistantPrompt.getSqlPrompt(sqlprompt);
            rspText = ChatClient.builder(this.chatModel).build().prompt(prompt).call().content();
        }
        String sql = SqlExtractorUtils.extractSql(rspText);
        return validSql(sql) ? sql : null;
    }

    private boolean validSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            log.error("SQL cannot be null or empty");
            return false;
        }
        return true;
    }
    @Transactional(readOnly = true)
    public List<Map<String, Object>> executeSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }
        try {
            log.info("Begin to execute the SQL: {}:{}", entityManager.isOpen(), sql);

            Query query = entityManager.createNativeQuery(sql);
            query.unwrap(org.hibernate.query.NativeQuery.class)
                    .setTupleTransformer((tuple, aliases) -> {
                        Map<String, Object> rowMap = new HashMap<>();
                        for (int i = 0; i < aliases.length; i++) {
                            rowMap.put(aliases[i], tuple[i]);
                        }
                        return rowMap;
                    });

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> resultList = query.getResultList();
            return resultList != null ? resultList : new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed to execute SQL: {}", sql, e);
            throw new RuntimeException("Failed to execute SQL", e);
        }
    }
    private List<Document> searchVectorByTag(String question, TrainingPolicy trainingPolicy) {
        try {
            FilterExpressionBuilder expression = new FilterExpressionBuilder();
            return this.vectorStore.similaritySearch(
                    SearchRequest.builder()
                            .query(question)
                            .similarityThreshold(0.2)
                            .topK(TOP_K)
                            .filterExpression(expression.eq("script_type", trainingPolicy.name()).build())
                            .build()
            );
        } catch (Exception e) {
            // 记录日志并返回空列表，具体处理方式根据业务需求调整
            log.error("Error searching documents[trainingPolicy={};error={}]", trainingPolicy , e.getMessage(),e);
            return Collections.emptyList();
        }
    }

}

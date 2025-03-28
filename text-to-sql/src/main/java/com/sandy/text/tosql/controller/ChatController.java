package com.sandy.text.tosql.controller;

import com.sandy.text.tosql.model.SqlAssistantPrompt;
import com.sandy.text.tosql.model.SqlpromptBuilder;
import com.sandy.text.tosql.model.Training;
import com.sandy.text.tosql.model.TrainingPolicy;
import com.sandy.text.tosql.request.ChatReq;
import com.sandy.text.tosql.request.TrainingReq;
import com.sandy.text.tosql.response.ResultRsp;
import com.sandy.text.tosql.util.SqlExtractorUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("sandy")
@AllArgsConstructor
public class ChatController {
    private static final int TOP_K = 10;
    @Autowired
    private ChatModel chatModel; // 注入聊天模型

    @Autowired
    private final VectorStore vectorStore;

    @PersistenceContext
    private EntityManager entityManager;


    protected ResourceLoader resourceLoader;


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

    @PostMapping("/chat")
    public ResponseBodyEmitter chat(@RequestBody ChatReq chatReq, HttpServletResponse servletResponse) throws IOException {
        log.info("This a request to chat[trainingReq={}].",JsonParser.toJson(chatReq));
        ResponseBodyEmitter emitter = new ResponseBodyEmitter(5*60*1000L);
        AtomicReference<String> sql= new AtomicReference<>();
        AtomicReference<Object> object = new AtomicReference<>();
        AtomicReference<String> htmlfile= new AtomicReference<>();
        servletResponse.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        CompletableFuture.runAsync(() -> {
            try {
                doSend(emitter,"收到您的问题，正快马加鞭进行解决<br>生成SQL中...");
                sql.set(generateSql(chatReq.getQuestion()));
                doSend(emitter,"<br>生成SQL：" + sql+"<br>查询数据中...");
                object.set(executeSql(sql.get()));
                doSend(emitter,"<br>查询到数据：" + JsonParser.toJson(object)+"<br>生成图表中...");
                htmlfile.set(generateHtml(chatReq.getQuestion(), object));
                doSend(emitter,"<br><iframe src=\"" + htmlfile + "\" width=\"100%\" height=\"100%\" frameborder=\"0\" title=\"" + chatReq.getQuestion() + "\"></iframe>");
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        }, executorService);
        return emitter;
    }

    private void doSend(ResponseBodyEmitter emitter, String msg) throws IOException {
        log.info("send:{}",msg);
        emitter.send(msg);
    }

    private String generateHtml(String question, AtomicReference<Object> data) {
        try {

            Resource resource = resourceLoader.getResource("classpath:template.html");
            // 读取 JSON 文件内容到字符串
            String template = null;
            try {
                template = new String(toByteArray(resource.getInputStream()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            PromptTemplate promptTemplate = new PromptTemplate("""
                    ### 目标
                    基于给定的title、data和html模版，生成html，将html模版里的$title替换为给定的title，基于data生成echarts脚本，并将html模版里的$generateScript替换为生成的echarts脚本,要求只返回html部分，并且去掉```html和```，将html部分生成html文件后，可以被浏览器正常访问
                    ### 给定的title为
                    {title}
                    ### 给定的data为
                    {data}
                    ### 给定的模版为
                    {template}
                    """);

            Prompt prompt = promptTemplate.create(Map.of("title", question,"data",JsonParser.toJson(data), "template", template));
            log.info("prompt:{}", JsonParser.toJson(prompt));
            String content =ChatClient.builder(chatModel) .build().prompt(prompt).call().content();
            content=null!=content?content.replaceAll("```html","").replaceAll("```",""):null;
            String directory = "./generated/";
//            String directory = "/Users/mark/work/gitspace/opensource/sandy-apps/duosql/super-sql-ui/";
            String fileName = UUID.randomUUID().toString() + ".html";
            File file = new File(directory+fileName);
            // 写入文件
            try (FileWriter writer = new FileWriter(file)) {
                String[] lines = content.split("\\r?\\n"); // 将字符串按行拆分，兼容不同换行符
                boolean startWriting = false; // 标记是否开始写入
                for (String line : lines) {
                    // 检查是否到达开始写入的行
                    if (line.trim().startsWith("<!DOCTYPE html>")) {
                        startWriting = true;
                    }
                    // 如果已经开始写入，则写入当前行
                    if (startWriting) {
                        writer.write(line);
                        writer.write(System.lineSeparator()); // 添加换行符
                        // 检查是否到达结束行
                        if (line.trim().endsWith("</html>")) {
                            break; // 遇到 </html> 后结束
                        }
                    }
                }
            }
            log.info("generate html fileName:{},file:{}",fileName,content);
            String address = "http://localhost:8081/"+fileName;
            return address;
        }catch (Exception e){
            log.error("Failed to generate EchartsJson: {}", e.getMessage());
        }
        return null;
    }


    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try(InputStream in = input){
            IOUtils.copy(in, output);
        }
        return output.toByteArray();
    }

    private String generateSql(String question) {
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
    private List<Map<String, Object>> executeSql(String sql) {
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

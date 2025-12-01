# 第9期：Text-to-SQL实战 - 让业务人员用自然语言查数据库

## 📌 本期概述

**核心问题：如何让不懂SQL的业务人员也能轻松查询数据库？**

Text-to-SQL技术可以将自然语言问题转换为标准SQL查询，彻底打破技术门槛。本期将深入讲解Schema动态注入、Few-shot训练、SQL安全执行等核心技术，并实现流式输出和ECharts数据可视化的完整方案。

## 🎯 学习目标

完成本期学习后，你将能够：
- ✅ 理解Text-to-SQL技术原理和完整工作流程
- ✅ 实现数据库Schema的智能注入
- ✅ 设计高质量的SQL生成Prompt
- ✅ 掌握Few-shot训练样本管理
- ✅ 实现SQL安全执行和结果返回
- ✅ 处理SQL注入等安全问题

## 📚 内容大纲

### 1. Text-to-SQL技术原理

### 2. 项目架构与核心组件

### 3. Schema智能注入

### 4. Few-shot训练样本管理

### 5. SQL生成与执行

### 6. 安全防护策略

---

## 1. Text-to-SQL技术原理

### 1.1 什么是Text-to-SQL？

**Text-to-SQL** 是将自然语言问题转换为SQL查询的技术。

**传统方式的痛点**：

```
业务人员："查一下上个月销售额最高的前10个产品"

传统做法：
1. 找技术人员 ❌
2. 描述需求 ❌
3. 等待开发 ❌
4. 等待SQL查询 ❌
5. 得到结果 ✅（但太慢了！）

Text-to-SQL：
1. 输入问题 ✅
2. AI生成SQL ✅
3. 立即得到结果 ✅（秒级响应！）
```

### 1.2 Text-to-SQL工作流程

```
用户问题："上个月销售额最高的前10个产品"
    ↓
【Step 1】Schema注入
  从数据库获取相关表结构：
  - orders表：order_id, product_id, amount, create_time
  - products表：product_id, product_name, category
    ↓
【Step 2】Few-shot检索
  从向量数据库检索相似问题：
  - "查询销售额前5的产品" → SELECT ... TOP 5
  - "上个月的订单统计" → WHERE create_time >= ...
    ↓
【Step 3】构建Prompt
  系统提示 + DDL + 示例 + 用户问题
    ↓
【Step 4】LLM生成SQL
  SELECT p.product_name, SUM(o.amount) as total_sales
  FROM orders o
  JOIN products p ON o.product_id = p.product_id
  WHERE o.create_time >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
  GROUP BY p.product_id, p.product_name
  ORDER BY total_sales DESC
  LIMIT 10
    ↓
【Step 5】执行SQL
  安全验证 → 执行查询 → 返回结果
    ↓
【Step 6】结果展示
  表格 / 图表 / 自然语言描述
```

### 1.3 核心挑战

| 挑战 | 说明 | 解决方案 |
|------|------|---------|
| **Schema理解** | 数据库可能有几百个表 | 向量检索相关表结构 |
| **SQL准确性** | 生成的SQL可能有语法错误 | Few-shot示例 + 语法验证 |
| **性能问题** | 复杂查询可能很慢 | 限制查询复杂度 |
| **安全问题** | 可能生成危险SQL | 白名单 + 参数校验 |
| **业务术语** | "销冠"、"本月" | 术语词典 + 文档注入 |

---

## 2. 项目架构与核心组件

现在开始构建完整的Text-to-SQL系统！

项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/text-to-sql](https://github.com/Mark7766/spring-ai-apps/tree/main/text-to-sql)

### 2.1 项目依赖配置

```xml
<!-- 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/text-to-sql/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.4</version>
    </parent>
    
    <groupId>com.sandy</groupId>
    <artifactId>text-to-sql</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0-M6</spring-ai.version>
        <spring-ai-alibaba.version>1.0.0-M6.1</spring-ai-alibaba.version>
    </properties>
    
    <dependencies>
        <!-- ⭐ Spring AI阿里云（通义千问） -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter</artifactId>
            <version>${spring-ai-alibaba.version}</version>
        </dependency>
        
        <!-- Spring Boot Web + Thymeleaf -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        
        <!-- ⭐ Spring Data JPA（数据库操作） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        
        <!-- ⭐ Chroma向量数据库（存储训练样本） -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-chroma-store-spring-boot-starter</artifactId>
        </dependency>
        
        <!-- ⭐ Ollama本地模型（可选） -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
        </dependency>
        
        <!-- 数据库驱动 -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
    
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.ai</groupId>
                <artifactId>spring-ai-bom</artifactId>
                <version>${spring-ai.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

**核心依赖说明**：

1. **spring-ai-alibaba-starter**：阿里云通义千问，SQL生成质量高
2. **spring-data-jpa**：数据库操作和Schema提取
3. **spring-ai-chroma**：向量数据库，存储训练样本
4. **mysql/postgresql**：支持多种数据库

### 2.2 核心架构

```
┌─────────────────────────────────────┐
│         用户界面 (Thymeleaf)         │
│     输入：自然语言问题                │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│      ChatController (API层)         │
│  - 接收用户问题                      │
│  - 流式返回结果                      │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│       DataService (业务层)          │
│  - SQL生成                           │
│  - SQL执行                           │
│  - 结果格式化                        │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│        DbService (SQL生成)          │
│  - Schema注入                        │
│  - Few-shot检索                      │
│  - Prompt构建                        │
│  - LLM调用                           │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│     VectorStore (训练样本库)        │
│  - DDL（表结构）                     │
│  - SQL示例                           │
│  - 业务文档                          │
└─────────────────────────────────────┘
```

---

## 3. Schema智能注入

### 3.1 为什么需要Schema注入？

AI需要知道数据库结构才能生成正确的SQL：

```
❌ 没有Schema：
用户："查询订单表"
AI："SELECT * FROM orders"
问题：不知道表名是orders还是order_info？有哪些字段？

✅ 有Schema：
用户："查询订单表"
AI看到DDL：
  CREATE TABLE orders (
    order_id INT PRIMARY KEY,
    customer_name VARCHAR(100),
    amount DECIMAL(10,2),
    create_time DATETIME
  )
AI："SELECT order_id, customer_name, amount FROM orders"
```

### 3.2 Prompt构建器

创建`SqlAssistantPrompt.java`，负责构建完整的Prompt：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/text-to-sql/src/main/java/com/sandy/text/tosql/model/SqlAssistantPrompt.java（第1/2部分）
package com.sandy.text.tosql.model;

import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;

import java.util.*;

public class SqlAssistantPrompt {

    // ⭐ 系统提示词
    private static final String INITIAL_PROMPT = """
        ### Goal
        You are a SQL expert.
        Please help to generate a SQL query to answer the question. 
        Your response should ONLY be based on the given context and follow the response guidelines and format instructions.
        """;
    
    // ⭐ 响应规则
    private static final String RESPONSE_GUIDELINES = """
        ### Response Guidelines
        1. If the provided context is sufficient, please generate a valid SQL query without any explanations for the question.
        2. If the provided context is almost sufficient but requires knowledge of a specific string in a particular column, please generate an intermediate SQL query to find the distinct strings in that column. Prepend the query with a comment saying intermediate_sql
        3. If the provided context is insufficient, please explain why it can't be generated.
        4. Please use the most relevant table(s).
        5. If the question has been asked and answered before, please repeat the answer exactly as it was given before.
        6. Ensure that the output SQL is SQL-compliant and executable, and free of syntax errors.
        7. 查询sql的where条件里，不能进行翻译，提问里是英文就是英文，是中文就是中文
        """;

    private static final int MAX_LENGTH = 14000;  // Token限制

    /**
     * ⭐ 构建完整的SQL生成Prompt
     */
    public static Prompt getSqlPrompt(SqlpromptBuilder sqlprompt) {
        if (sqlprompt == null) {
            throw new IllegalArgumentException("Sqlprompt cannot be null");
        }
        
        // Step 1: 添加DDL（表结构）
        String initialPrompt = addDdlToPrompt(
            INITIAL_PROMPT, 
            sqlprompt.getDdlList(), 
            MAX_LENGTH
        );
        
        // Step 2: 添加业务文档
        initialPrompt = addDocumentationToPrompt(
            initialPrompt, 
            sqlprompt.getDocumentList(), 
            MAX_LENGTH
        );
        
        // Step 3: 添加响应规则
        initialPrompt += RESPONSE_GUIDELINES;

        // Step 4: 生成消息列表（包含Few-shot示例）
        return generateMessageLog(
            initialPrompt, 
            sqlprompt.getQuestionSqlList(), 
            sqlprompt.getQuestion()
        );
    }

    /**
     * ⭐ 添加DDL到Prompt
     */
    private static String addDdlToPrompt(
        String initialPrompt, 
        List<Document> ddlList, 
        int maxTokens
    ) {
        StringBuilder promptBuilder = new StringBuilder(initialPrompt);
        
        if (!ddlList.isEmpty()) {
            promptBuilder.append("\n### Tables \n");
            int currentTokenCount = strToApproxTokenCount(promptBuilder.toString());
            
            for (Document ddl : ddlList) {
                int ddlTokenCount = strToApproxTokenCount(ddl.getText());
                
                // Token限制检查
                if (currentTokenCount + ddlTokenCount < maxTokens) {
                    promptBuilder.append(ddl.getText()).append("\n\n");
                    currentTokenCount += ddlTokenCount + 2;
                }
            }
        }
        
        return promptBuilder.toString();
    }

    /**
     * ⭐ 添加业务文档到Prompt
     */
    private static String addDocumentationToPrompt(
        String initialPrompt, 
        List<Document> documentationList, 
        int maxTokens
    ) {
        StringBuilder promptBuilder = new StringBuilder(initialPrompt);
        int currentTokenCount = strToApproxTokenCount(initialPrompt);
        
        if (!documentationList.isEmpty()) {
            promptBuilder.append("\n### Additional Context \n\n");
            currentTokenCount += 2;
            
            for (Document documentation : documentationList) {
                int docTokenCount = strToApproxTokenCount(documentation.getText());
                
                if (currentTokenCount + docTokenCount < maxTokens) {
                    promptBuilder.append(documentation.getText()).append("\n\n");
                    currentTokenCount += docTokenCount + 2;
                }
            }
        }
        
        return promptBuilder.toString();
    }

    /**
     * Token计算（简化版）
     */
    private static int strToApproxTokenCount(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }
        return str.length() / 4;  // 粗略估算：4字符≈1 token
    }
}
```

继续Prompt构建的Few-shot部分：

```java
    // 继续：https://github.com/Mark7766/spring-ai-apps/blob/main/text-to-sql/src/main/java/com/sandy/text/tosql/model/SqlAssistantPrompt.java（第2/2部分）
    
    /**
     * ⭐ 生成消息列表（包含Few-shot示例）
     */
    private static Prompt generateMessageLog(
        String initialPrompt, 
        List<Document> questionSqlList, 
        String question
    ) {
        if (question == null || question.isEmpty()) {
            throw new IllegalArgumentException("Question cannot be null or empty");
        }

        // 系统消息
        Message systemMessage = new SystemMessage(initialPrompt);
        // 用户消息
        Message userMessage = new UserMessage(question);

        List<Message> messages = new ArrayList<>(List.of(systemMessage));
        
        // ⭐ 添加Few-shot示例（问题-SQL对）
        if (!questionSqlList.isEmpty()) {
            for (Document document : questionSqlList) {
                try {
                    Training training = JsonParser.fromJson(
                        document.getText(), 
                        Training.class
                    );
                    
                    if (StringUtils.hasText(training.getContent()) && 
                        StringUtils.hasText(training.getQuestion())) {
                        // 添加用户问题示例
                        messages.add(new UserMessage(training.getQuestion()));
                        // 添加AI回复示例（SQL）
                        messages.add(new AssistantMessage(training.getContent()));
                    }
                } catch (Exception e) {
                    logger.warn("Error processing document: {}", document, e);
                }
            }
        }
        
        // 最后添加当前用户问题
        messages.add(userMessage);
        
        return new Prompt(messages);
    }
```

**Prompt结构示例**：

```
System: [系统提示 + DDL + 业务文档 + 响应规则]

### Tables
CREATE TABLE orders (
  order_id INT,
  product_id INT,
  amount DECIMAL(10,2),
  create_time DATETIME
)

### Additional Context
业务规则：上个月 = MONTH(create_time) = MONTH(DATE_SUB(NOW(), INTERVAL 1 MONTH))

### Response Guidelines
1. 生成有效的SQL查询...

User: 查询销售额前5的产品
Assistant: SELECT product_id, SUM(amount) FROM orders GROUP BY product_id ORDER BY SUM(amount) DESC LIMIT 5

User: 上个月的订单数量
Assistant: SELECT COUNT(*) FROM orders WHERE MONTH(create_time) = MONTH(DATE_SUB(NOW(), INTERVAL 1 MONTH))

User: 上个月销售额最高的前10个产品
```

---

## 4. Few-shot训练样本管理

### 4.1 训练样本类型

系统支持3种训练样本：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/text-to-sql/src/main/java/com/sandy/text/tosql/model/TrainingPolicy.java
public enum TrainingPolicy {
    DDL,           // 表结构定义
    SQL,           // 问题-SQL示例对
    DOCUMENTATION  // 业务文档
}
```

### 4.2 向量检索训练样本

在`DbService.java`中实现：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/text-to-sql/src/main/java/com/sandy/text/tosql/service/DbService.java（部分）
@Service
public class DbService {
    
    private static final int TOP_K = 5;  // 检索Top 5最相关
    
    @Autowired
    private VectorStore vectorStore;

    /**
     * ⭐ 根据标签向量检索
     */
    private List<Document> searchVectorByTag(
        String question, 
        TrainingPolicy trainingPolicy
    ) {
        try {
            FilterExpressionBuilder expression = new FilterExpressionBuilder();
            
            return this.vectorStore.similaritySearch(
                SearchRequest.builder()
                    .query(question)
                    .similarityThreshold(0.2)  // 相似度阈值
                    .topK(TOP_K)                // Top 5
                    .filterExpression(
                        expression.eq("script_type", trainingPolicy.name()).build()
                    )
                    .build()
            );
        } catch (Exception e) {
            log.error("Error searching documents: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
```

**检索逻辑**：

```
用户问题："上个月销售额最高的产品"
    ↓
【检索DDL】script_type=DDL
  → orders表结构
  → products表结构
    ↓
【检索SQL示例】script_type=SQL
  → "上个月的订单" → WHERE MONTH(create_time) = ...
  → "销售额最高" → ORDER BY SUM(amount) DESC
    ↓
【检索业务文档】script_type=DOCUMENTATION
  → "上个月"的定义
  → "销售额"的计算规则
```

---

## 5. SQL生成与执行

### 5.1 SQL生成核心逻辑

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/text-to-sql/src/main/java/com/sandy/text/tosql/service/DbService.java
@Service
public class DbService {
    
    @Autowired
    private ChatModel chatModel;
    
    @Autowired
    private VectorStore vectorStore;
    
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * ⭐ 生成SQL（核心方法）
     */
    public String generateSql(String question) {
        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("Question cannot be null or empty");
        }
        
        // ⭐ Step 1: 向量检索相关内容
        List<Document> questionSqlList = this.searchVectorByTag(question, TrainingPolicy.SQL);
        List<Document> ddlList = this.searchVectorByTag(question, TrainingPolicy.DDL);
        List<Document> documentList = this.searchVectorByTag(question, TrainingPolicy.DOCUMENTATION);
        
        // ⭐ Step 2: 构建SqlPrompt
        SqlpromptBuilder sqlprompt = SqlpromptBuilder.builder()
            .question(question)
            .questionSqlList(questionSqlList)
            .ddlList(ddlList)
            .documentList(documentList)
            .build();
        
        // ⭐ Step 3: 生成Prompt
        Prompt prompt = SqlAssistantPrompt.getSqlPrompt(sqlprompt);
        log.info("Generating SQL Prompt:\n{}", prompt.getContents());
        
        // ⭐ Step 4: 调用LLM生成SQL
        ChatResponse llmResponse = ChatClient.builder(chatModel)
            .build()
            .prompt(prompt)
            .call()
            .chatResponse();
        
        log.info("LLM Response: {}", JsonParser.toJson(llmResponse));
        String rspText = llmResponse.getResult().getOutput().getText();
        
        // ⭐ Step 5: 处理中间SQL（如果需要）
        if (rspText.contains("intermediate_sql")) {
            String intermediateSql = SqlExtractorUtils.extractSql(rspText);
            List<Map<String, Object>> executed = executeSql(intermediateSql);
            
            // 将中间结果添加到文档
            sqlprompt.getDocumentList().add(
                new Document(String.format("""
                    The following is a pandas DataFrame with the results of the intermediate SQL query %s:
                    %s
                    """, intermediateSql, executed.toString()
                ))
            );
            
            // 重新生成Prompt并调用LLM
            prompt = SqlAssistantPrompt.getSqlPrompt(sqlprompt);
            rspText = ChatClient.builder(this.chatModel)
                .build()
                .prompt(prompt)
                .call()
                .content();
        }
        
        // ⭐ Step 6: 提取最终SQL
        String sql = SqlExtractorUtils.extractSql(rspText);
        return validSql(sql) ? sql : null;
    }

    /**
     * ⭐ 执行SQL
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> executeSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL cannot be null or empty");
        }
        
        try {
            log.info("Execute SQL: {}", sql);
            
            Query query = entityManager.createNativeQuery(sql);
            
            // ⭐ 设置Tuple转换器，将结果转为Map
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
            log.error("Failed to execute SQL: {}", sql, e);
            throw new RuntimeException("Failed to execute SQL", e);
        }
    }

    /**
     * SQL有效性验证
     */
    private boolean validSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            log.error("SQL cannot be null or empty");
            return false;
        }
        // 这里可以添加更多验证逻辑
        return true;
    }
}
```

**SQL生成流程**：

```
问题："上个月销售额最高的前10个产品"
    ↓
【向量检索】
  - DDL: orders表, products表
  - SQL示例: "上个月" → WHERE MONTH(...)
  - 文档: 销售额定义
    ↓
【构建Prompt】
  System + DDL + 示例 + 文档 + 问题
    ↓
【LLM生成】
  "SELECT p.product_name, SUM(o.amount) as sales
   FROM orders o JOIN products p ...
   WHERE MONTH(o.create_time) = MONTH(DATE_SUB(NOW(), INTERVAL 1 MONTH))
   GROUP BY p.product_id
   ORDER BY sales DESC
   LIMIT 10"
    ↓
【提取SQL】
  使用SqlExtractorUtils提取纯SQL
    ↓
【验证SQL】
  语法检查 + 安全检查
```

### 5.2 中间SQL处理

当AI不确定某些值时，会先生成中间SQL查询：

```
用户："查询iPhone的销售额"
AI不知道：产品名称到底是"iPhone"、"iphone"还是"IPHONE"？

Step 1: 生成中间SQL
  -- intermediate_sql
  SELECT DISTINCT product_name FROM products WHERE product_name LIKE '%iphone%'
  
Step 2: 执行中间SQL
  结果：["iPhone 14", "iPhone 15", "iPhone SE"]
  
Step 3: 将结果注入Prompt
  "The following products match 'iPhone': iPhone 14, iPhone 15, iPhone SE"
  
Step 4: 重新生成最终SQL
  SELECT SUM(amount) FROM orders 
  WHERE product_name IN ('iPhone 14', 'iPhone 15', 'iPhone SE')
```

---

## 6. 安全防护策略

### 6.1 SQL注入防护

**问题**：恶意用户可能输入危险问题

```
❌ 危险输入：
"删除所有订单; DROP TABLE users;"

可能生成：
DELETE FROM orders;
DROP TABLE users;
```

**防护措施**：

```java
// SQL验证和清理
public boolean isSafeSql(String sql) {
    // 1. 白名单检查：只允许SELECT
    if (!sql.trim().toUpperCase().startsWith("SELECT")) {
        log.error("Only SELECT queries are allowed");
        return false;
    }
    
    // 2. 黑名单检查：禁止危险关键词
    String[] dangerousKeywords = {
        "DROP", "DELETE", "UPDATE", "INSERT", 
        "EXEC", "EXECUTE", "TRUNCATE", "ALTER"
    };
    
    String upperSql = sql.toUpperCase();
    for (String keyword : dangerousKeywords) {
        if (upperSql.contains(keyword)) {
            log.error("Dangerous keyword detected: {}", keyword);
            return false;
        }
    }
    
    // 3. 注释检查
    if (sql.contains("--") || sql.contains("/*")) {
        log.error("Comments not allowed in SQL");
        return false;
    }
    
    return true;
}
```

### 6.2 查询限制

```java
// 限制查询结果数量
public List<Map<String, Object>> executeSql(String sql) {
    // 自动添加LIMIT
    if (!sql.toUpperCase().contains("LIMIT")) {
        sql += " LIMIT 1000";  // 最多返回1000条
    }
    
    // 设置超时
    Query query = entityManager.createNativeQuery(sql);
    query.setHint("javax.persistence.query.timeout", 5000);  // 5秒超时
    
    return query.getResultList();
}
```

### 6.3 权限控制

```java
// 使用只读数据库用户
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: readonly_user  # ⭐ 只读账号
    password: xxx
```

### 6.4 SQL审计日志

```java
@Aspect
@Component
public class SqlAuditAspect {
    
    @Around("execution(* com.sandy.text.tosql.service.DbService.executeSql(..))")
    public Object auditSql(ProceedingJoinPoint joinPoint) throws Throwable {
        String sql = (String) joinPoint.getArgs()[0];
        String user = getCurrentUser();
        
        // 记录审计日志
        log.info("SQL Audit: user={}, sql={}", user, sql);
        
        // 写入审计表
        auditRepository.save(new SqlAudit(user, sql, LocalDateTime.now()));
        
        return joinPoint.proceed();
    }
}
```

### 6.5 安全检查清单

- [ ] 只允许SELECT查询
- [ ] 禁止DROP、DELETE等危险操作
- [ ] 自动添加LIMIT限制
- [ ] 设置查询超时
- [ ] 使用只读数据库账号
- [ ] 记录SQL审计日志
- [ ] 验证表名和字段名
- [ ] 过滤注释和特殊字符

---

## 💻 示例代码

完整项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/text-to-sql](https://github.com/Mark7766/spring-ai-apps/tree/main/text-to-sql)

**项目结构**：
```
text-to-sql/
├── src/main/java/com/sandy/text/tosql/
│   ├── TextToSqlApplication.java          # 启动类
│   ├── controller/
│   │   ├── ChatController.java             # API控制器
│   │   └── TrainingController.java         # 训练样本管理
│   ├── service/
│   │   ├── DbService.java                  # SQL生成核心
│   │   ├── DataService.java                # 业务编排
│   │   └── HtmlService.java                # 结果格式化
│   ├── model/
│   │   ├── SqlAssistantPrompt.java         # Prompt构建
│   │   ├── SqlpromptBuilder.java           # Prompt Builder
│   │   ├── Training.java                   # 训练样本模型
│   │   └── TrainingPolicy.java             # 样本类型枚举
│   └── util/
│       └── SqlExtractorUtils.java          # SQL提取工具
├── src/main/resources/
│   ├── application.yml                     # 配置文件
│   └── templates/                          # Thymeleaf模板
└── pom.xml
```

**核心文件**：
- **DbService.java**：SQL生成和执行核心逻辑（150行）
- **SqlAssistantPrompt.java**：Prompt构建器（200行）
- **ChatController.java**：流式API接口

---

## 🤔 思考题

1. **如何处理复杂的JOIN查询和子查询？**
   
   提示：Few-shot训练样本中包含复杂SQL示例，AI会学习这些模式。

2. **如何让AI理解业务术语（如"销冠"、"TOP10"）？**
   
   提示：使用DOCUMENTATION类型的训练样本，定义业务术语词典。

3. **Text-to-SQL系统如何保证查询性能？**
   
   提示：添加LIMIT限制、设置超时、使用索引提示、缓存常见查询。

---

## 📖 拓展阅读

- [Text-to-SQL研究进展](https://arxiv.org/abs/2208.13629)
- [Spring Data JPA文档](https://docs.spring.io/spring-data/jpa/reference/)
- [SQL注入防护最佳实践](https://owasp.org/www-community/attacks/SQL_Injection)
- [Few-shot Learning原理](https://arxiv.org/abs/2005.14165)

---

## ⏭️ 下期预告

恭喜你掌握了Text-to-SQL技术！🎉 现在业务人员可以用自然语言查数据库了。

**下一期我们将学习模板化AI生成**，让AI快速创建原型设计和结构化内容，实现从数据库到前端页面的全自动生成！

**下期亮点**：
- 🎨 AI驱动的原型设计
- 📄 结构化文档生成
- 🏗️ 代码框架自动生成
- 🎯 模板引擎集成
- 🔄 可视化页面生成

敬请期待！

---

**更新日期**：2025年12月3日  
**状态**：✅ 已完成


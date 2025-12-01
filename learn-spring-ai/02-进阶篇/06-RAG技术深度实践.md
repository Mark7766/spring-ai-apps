# 第6期：RAG技术深度实践 - 让AI基于你的数据回答问题

## 📌 本期概述

**核心问题：如何让AI准确回答基于企业私有数据的问题？**

RAG（Retrieval-Augmented Generation，检索增强生成）是目前最实用的AI应用技术之一。上一期我们实现了PDF文档的向量化存储，本期将深入讲解完整的RAG流程，使用Spring AI的ETL Pipeline实现企业级多格式文档问答系统。

## 🎯 学习目标

完成本期学习后，你将能够：
- ✅ 深入理解RAG技术原理和工作流程
- ✅ 掌握Spring AI的ETL Pipeline架构
- ✅ 实现多格式文档处理（PDF、Word、PPT、TXT）
- ✅ 设计科学的文档分片策略
- ✅ 掌握Prompt工程最佳实践
- ✅ 评估和优化RAG答案质量

## 📚 内容大纲

### 1. RAG技术原理

### 2. Spring AI ETL Pipeline详解

### 3. 多格式文档处理实战

### 4. 文档分片策略深度解析

### 5. Prompt工程最佳实践

### 6. 答案质量评估与优化

---

## 1. RAG技术原理

### 1.1 什么是RAG？

**RAG（Retrieval-Augmented Generation）** = 检索 + 生成

```
传统LLM：
用户提问 → LLM直接回答
问题：只能基于训练数据，无法获取最新信息

RAG增强：
用户提问 → 检索相关文档 → 结合文档内容 → LLM生成答案
优势：可以基于私有数据、实时数据回答
```

### 1.2 RAG vs 微调

| 对比维度 | RAG | 微调（Fine-tuning） |
|---------|-----|-------------------|
| **成本** | 低（只需向量化） | 高（需要GPU训练） |
| **速度** | 快（即时部署） | 慢（需训练周期） |
| **数据更新** | 实时（增删文档即可） | 困难（需重新训练） |
| **适用场景** | 知识问答、文档检索 | 特定领域、风格迁移 |
| **数据量要求** | 少（几百篇文档） | 大（千万级数据） |

**RAG的优势**：
- ✅ 可解释性强（可追溯到源文档）
- ✅ 数据更新灵活
- ✅ 成本低，易部署
- ✅ 适合企业私有知识库

### 1.3 RAG完整工作流程

```
【离线阶段：文档准备】
1. 文档上传（PDF、Word、PPT等）
2. 文档解析（提取文本）
3. 文档分片（切分成小块）
4. 向量化（Embedding）
5. 存储到向量数据库

【在线阶段：问答】
1. 用户提问
2. 问题向量化
3. 相似度搜索（检索Top-K文档片段）
4. 构建Prompt（问题+文档片段）
5. LLM生成答案
6. 返回给用户
```

**可视化流程**：

```
文档上传
    ↓
【ETL Pipeline】
  ├─ Extract（提取）→ 解析PDF/Word/PPT
  ├─ Transform（转换）→ 分片、清洗
  └─ Load（加载）→ 向量化+存储到Chroma
    ↓
用户提问
    ↓
【RAG检索】
  ├─ 向量化问题
  ├─ 相似度搜索（Top-K）
  └─ 获取相关文档片段
    ↓
【Prompt构建】
  "基于以下内容回答问题：
   [文档片段1]
   [文档片段2]
   ...
   问题：{用户问题}"
    ↓
【LLM生成】
  ChatModel.call(prompt)
    ↓
返回答案
```

---

## 2. Spring AI ETL Pipeline详解

### 2.1 ETL Pipeline架构

Spring AI提供了完整的ETL（Extract-Transform-Load）Pipeline：

```
┌─────────────────────────────────────┐
│        DocumentReader               │  Extract（提取）
│  - PagePdfDocumentReader            │
│  - TikaDocumentReader               │
│  - TextReader                       │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│      DocumentTransformer            │  Transform（转换）
│  - TokenTextSplitter                │
│  - ContentFormatTransformer         │
│  - KeywordMetadataEnricher          │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│        VectorStore                  │  Load（加载）
│  - ChromaVectorStore                │
│  - PineconeVectorStore              │
│  - PgVectorStore                    │
└─────────────────────────────────────┘
```

**核心组件**：

1. **DocumentReader**：从各种格式文件中提取文本
2. **DocumentTransformer**：对文档进行转换（分片、清洗等）
3. **VectorStore**：向量化并存储到数据库

### 2.2 ETL Pipeline实战

现在开始构建完整的多格式文档问答系统！

项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/etl](https://github.com/Mark7766/spring-ai-apps/tree/main/etl)

---

## 3. 多格式文档处理实战

### 3.1 项目依赖配置

```xml
<!-- 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/etl/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.4</version>
    </parent>
    
    <groupId>com.sandy</groupId>
    <artifactId>etl</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    <description>支持各类文件的上传与向量化存储，以进行检索增强</description>
    
    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web + Thymeleaf -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>
        
        <!-- ⭐ Spring AI PDF文档读取器 -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-pdf-document-reader</artifactId>
        </dependency>
        
        <!-- ⭐ Spring AI Tika文档读取器（支持Word、PPT等） -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-tika-document-reader</artifactId>
        </dependency>
        
        <!-- Spring AI Ollama -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-ollama</artifactId>
        </dependency>
        
        <!-- Spring AI Chroma向量数据库 -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-vector-store-chroma</artifactId>
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

1. **spring-ai-pdf-document-reader**：PDF文档解析
2. **spring-ai-tika-document-reader**：支持Word、PPT、Excel等多种格式
3. **其他依赖**：Ollama模型、Chroma向量数据库

### 3.2 应用配置

```yaml
# 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/etl/src/main/resources/application.yml
server:
  port: 8081

spring:
  application:
    name: "etl"
  
  thymeleaf:
    cache: false
  
  # ⭐ 文件上传配置（支持大文件）
  servlet:
    multipart:
      max-file-size: 100MB      # 单文件最大100MB
      max-request-size: 100MB   # 请求最大100MB
  
  ai:
    ollama:
      base-url: "http://localhost:11434"
      embedding:
        enabled: true
        model: qwen2.5
      chat:
        enabled: true
        model: qwen2.5
    
    # ⭐ Chroma向量数据库配置
    vectorstore:
      chroma:
        client:
          host: http://localhost
          port: 8000
        collection-name: etl-dev    # Collection名称
        initialize-schema: true      # 自动创建

logging:
  level:
    com.sandy.etl: DEBUG
```

**配置重点**：
- `multipart.max-file-size`：支持大文件上传
- `vectorstore.chroma`：Chroma连接配置
- `collection-name`：指定Collection名称

### 3.3 文档处理核心服务

创建`DocumentService.java`，实现多格式文档的ETL处理：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/etl/src/main/java/com/sandy/etl/DocumentService.java（第1/3部分）
package com.sandy.etl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileUrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    // 文档存储目录
    private static final String DIRECTORY = Paths.get(System.getProperty("user.dir"), "docs").toString() + "/";

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private TokenTextSplitter textSplitter;

    /**
     * ⭐ 上传并处理文件（ETL完整流程）
     */
    public void uploadAndProcessFile(MultipartFile file) throws IOException {
        // 1. 验证文件
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件为空");
        }

        // 2. 创建docs目录
        Path docsDir = Paths.get(DIRECTORY);
        if (!Files.exists(docsDir)) {
            Files.createDirectories(docsDir);
            logger.info("创建目录: {}", docsDir.toAbsolutePath());
        }

        // 3. 保存文件（带UUID避免重名）
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path targetFile = docsDir.resolve(fileName);
        File tmpFile = targetFile.toFile();
        
        try {
            file.transferTo(tmpFile);
            FileUrlResource resource = new FileUrlResource(tmpFile.getAbsolutePath());
            
            // ⭐ 4. Extract：根据文件类型选择DocumentReader
            List<Document> documents = extractDocuments(fileName, resource);
            
            // 验证文档内容
            if (documents.isEmpty()) {
                throw new IOException("文件内容为空或无法解析: " + fileName);
            }
            
            // ⭐ 5. Transform：添加元数据
            documents.forEach(doc -> {
                doc.getMetadata().put("filename", fileName);
                doc.getMetadata().put("version", "1");
                doc.getMetadata().put("type", "file");
            });
            
            // ⭐ 6. Transform：文档分片
            TokenTextSplitter splitter = new TokenTextSplitter(
                512,    // maxChunkSize：每片最大512 token
                128,    // chunkOverlap：重叠128 token
                100,    // minChunkSize：最小100 token
                100000, // maxChunkCharCount：最大字符数
                true    // keepSeparator：保留换行符
            );
            List<Document> splitDocuments = splitter.split(documents);
            
            // ⭐ 7. Load：存储到向量数据库
            vectorStore.add(splitDocuments);
            logger.info("成功处理文件: {}，分片数: {}", fileName, splitDocuments.size());
            
        } catch (Exception e) {
            // 清理失败的文件
            if (tmpFile.exists()) {
                Files.deleteIfExists(targetFile);
            }
            throw new IOException("处理文件失败: " + e.getMessage(), e);
        }
    }
```

继续文档处理的提取和删除逻辑：

```java
    // 继续：https://github.com/Mark7766/spring-ai-apps/blob/main/etl/src/main/java/com/sandy/etl/DocumentService.java（第2/3部分）
    
    /**
     * ⭐ Extract：根据文件类型提取文档
     */
    private List<Document> extractDocuments(String fileName, FileUrlResource resource) {
        String lowerCaseFileName = fileName.toLowerCase();
        
        // PDF文件
        if (lowerCaseFileName.endsWith(".pdf")) {
            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                resource,
                PdfDocumentReaderConfig.builder()
                    .withPageTopMargin(0)
                    .withPagesPerDocument(1)  // 每页一个Document
                    .build()
            );
            return pdfReader.read();
        }
        
        // 文本文件
        else if (lowerCaseFileName.endsWith(".txt")) {
            TextReader reader = new TextReader(resource);
            return reader.read();
        }
        
        // Word、PPT等（使用Tika）
        else if (lowerCaseFileName.endsWith(".doc") || 
                 lowerCaseFileName.endsWith(".docx") ||
                 lowerCaseFileName.endsWith(".ppt") || 
                 lowerCaseFileName.endsWith(".pptx")) {
            TikaDocumentReader reader = new TikaDocumentReader(resource);
            return reader.read();
        }
        
        else {
            throw new IllegalArgumentException("不支持的文件类型: " + fileName);
        }
    }

    /**
     * ⭐ 删除文件（从向量数据库和物理存储）
     */
    public void deleteFile(String fileName) {
        logger.info("删除文件: {}", fileName);
        
        // 1. 从向量数据库删除
        SearchRequest searchRequest = SearchRequest.builder()
            .query("*")  // 通配符查询
            .filterExpression(new FilterExpressionBuilder()
                .eq("filename", fileName)
                .build())
            .topK(1000)
            .similarityThreshold(0.0)  // 忽略相似度
            .build();
        
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        
        if (documents != null && !documents.isEmpty()) {
            List<String> docIds = documents.stream()
                .map(Document::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            
            if (!docIds.isEmpty()) {
                vectorStore.delete(docIds);
                logger.info("从Chroma删除 {} 个文档片段", docIds.size());
            }
        }
        
        // 2. 删除物理文件
        try {
            Path filePath = Paths.get(DIRECTORY, fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("删除物理文件: {}", filePath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("删除文件失败: " + fileName, e);
        }
    }

    /**
     * 列出所有文件
     */
    public List<String> listFiles() {
        SearchRequest searchRequest = SearchRequest.builder()
            .query("*:*")
            .topK(1000)
            .build();
        
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        
        return documents.stream()
            .map(doc -> (String) doc.getMetadata().get("filename"))
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
    }
}
```

**代码核心解析**：

1. **多格式支持**：
   - PDF → `PagePdfDocumentReader`
   - TXT → `TextReader`
   - Word/PPT → `TikaDocumentReader`

2. **ETL完整流程**：
   - **Extract**：`extractDocuments()`提取文本
   - **Transform**：添加元数据 + `TokenTextSplitter`分片
   - **Load**：`vectorStore.add()`存储

3. **文档分片参数**：
   ```java
   new TokenTextSplitter(
       512,    // 每片512 token（约400字）
       128,    // 重叠128 token（保持上下文）
       100,    // 最小100 token
       100000, // 最大10万字符
       true    // 保留换行符
   )
   ```

---

## 4. 文档分片策略深度解析

### 4.1 为什么需要分片？

**问题**：整个文档作为一个Document？

```
❌ 问题1：向量表达不精确
   "什么是RAG？" 匹配到100页的论文全文
   → 包含大量无关内容

❌ 问题2：超出Token限制
   LLM输入限制：GPT-4 8K，GPT-4-Turbo 128K
   一本书可能几百万字

❌ 问题3：检索效率低
   大文档向量相似度计算慢
```

**解决方案**：合理分片

```
✅ 精确检索：每片只包含特定主题
✅ 高效处理：控制每片大小
✅ 上下文保留：片段间有重叠
```

### 4.2 TokenTextSplitter详解

**TokenTextSplitter** 是Spring AI提供的基于Token的分片器：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/etl/src/main/java/com/sandy/etl/DocumentConfiguration.java
package com.sandy.etl;

import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentConfiguration {
    
    @Bean
    public TokenTextSplitter textSplitter() {
        return new TokenTextSplitter(
            512,    // defaultChunkSize：默认块大小
            128,    // minChunkSizeChars：最小块大小
            100,    // minChunkLengthToEmbed：最小嵌入长度
            100000, // maxNumChunks：最大块数
            true    // keepSeparator：保留分隔符
        );
    }
}
```

**参数说明**：

| 参数 | 默认值 | 说明 | 推荐值 |
|------|--------|------|--------|
| **maxChunkSize** | 800 | 每片最大Token数 | 200-800 |
| **chunkOverlap** | 0 | 片段重叠Token数 | 50-200 |
| **minChunkSize** | 350 | 最小片段大小 | 100-350 |
| **maxChunkCharCount** | 10000 | 最大字符数 | 50000-100000 |
| **keepSeparator** | false | 保留换行符 | true |

### 4.3 分片策略对比

**策略1：固定大小分片**

```java
TokenTextSplitter splitter = new TokenTextSplitter(500, 0);
// 优点：实现简单
// 缺点：可能在句子中间切断
```

**策略2：重叠分片（推荐）**

```java
TokenTextSplitter splitter = new TokenTextSplitter(500, 100);
// 优点：保留上下文，边界问题少
// 缺点：存储空间稍大
```

**示例**：

```
原文：
"RAG是检索增强生成技术。它结合了检索和生成两种方法。
检索部分负责找到相关文档，生成部分负责产生答案。"

不重叠分片（500 token）：
片段1："RAG是检索增强生成技术。它结合了检索和生成两种方法。"
片段2："检索部分负责找到相关文档，生成部分负责产生答案。"

重叠分片（500 token，100 token重叠）：
片段1："RAG是检索增强生成技术。它结合了检索和生成两种方法。"
片段2："它结合了检索和生成两种方法。检索部分负责找到相关文档，生成部分负责产生答案。"
       ↑────────── 重叠部分 ──────────↑
```

### 4.4 不同场景的分片策略

| 场景 | 推荐chunk大小 | 重叠大小 | 原因 |
|------|-------------|---------|------|
| **技术文档** | 300-500 token | 100 token | 保持代码完整性 |
| **新闻文章** | 400-800 token | 50 token | 段落相对独立 |
| **学术论文** | 500-1000 token | 150 token | 需要更多上下文 |
| **对话记录** | 按轮次 | 1轮 | 保持对话完整性 |
| **法律文档** | 200-400 token | 100 token | 精确引用 |

---

## 5. Prompt工程最佳实践

### 5.1 RAG的Prompt结构

创建`RagChatService.java`，实现RAG问答：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/etl/src/main/java/com/sandy/etl/RagChatService.java
package com.sandy.etl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RagChatService {
    
    @Autowired
    private ChatModel chatModel;
    
    @Autowired
    private VectorStore vectorStore;

    public String chat(String question) {
        log.info("question:{}", question);
        
        // ⭐ Step 1: 检索相关文档
        SearchRequest searchRequest = SearchRequest.builder()
            .query(question)
            .similarityThreshold(0.1)  // 相似度阈值
            .topK(10)                   // 返回Top 10
            .build();
        
        var documents = vectorStore.similaritySearch(searchRequest);
        
        // ⭐ Step 2: 构建Prompt
        StringBuilder prompt = new StringBuilder();
        prompt.append("基于以下内容回答问题：\n");
        
        documents.forEach(doc -> 
            prompt.append(doc.getText()).append("\n")
        );
        
        prompt.append("问题：").append(question);
        
        log.info("prompt:{}", prompt.toString());
        
        // ⭐ Step 3: 调用LLM生成答案
        ChatResponse chatResponse = chatModel.call(
            new Prompt(prompt.toString())
        );
        
        return chatResponse.getResult().getOutput().getText();
    }
}
```

**Prompt结构**：

```
基于以下内容回答问题：
[文档片段1]
[文档片段2]
...
问题：{用户问题}
```

### 5.2 高质量Prompt模板

**基础模板**（当前实现）：

```java
String prompt = "基于以下内容回答问题：\n" + context + "\n问题：" + question;
```

**优化模板1：明确角色**

```java
String prompt = """
你是一个专业的AI助手，负责基于提供的文档回答用户问题。

相关文档：
%s

用户问题：%s

请仔细阅读文档内容，如果文档中有明确答案，请引用原文；如果没有，请说明"文档中未找到相关信息"。
""".formatted(context, question);
```

**优化模板2：结构化输出**

```java
String prompt = """
基于以下文档片段回答问题。

文档内容：
%s

问题：%s

请按以下格式回答：
1. 直接答案：[简短回答]
2. 详细解释：[基于文档的详细说明]
3. 来源：[引用的文档片段]
""".formatted(context, question);
```

**优化模板3：Few-shot示例**

```java
String prompt = """
你是一个文档问答助手。参考以下示例格式回答：

示例1：
文档：Spring AI是一个AI应用开发框架。
问题：什么是Spring AI？
答案：根据文档，Spring AI是一个AI应用开发框架。

示例2：
文档：（空）
问题：什么是量子计算？
答案：抱歉，提供的文档中没有关于量子计算的信息。

现在，基于以下文档回答问题：

文档内容：
%s

用户问题：%s

你的答案：
""".formatted(context, question);
```

### 5.3 Prompt优化技巧

**技巧1：限制Token长度**

```java
// 只取前3000个字符，避免超出限制
String context = allDocuments.stream()
    .map(Document::getText)
    .collect(Collectors.joining("\n"))
    .substring(0, Math.min(3000, totalLength));
```

**技巧2：按相似度排序**

```java
// 相似度高的排在前面
documents.sort((a, b) -> 
    Double.compare(b.getScore(), a.getScore())
);
```

**技巧3：去重**

```java
// 移除重复内容
Set<String> uniqueContents = new HashSet<>();
documents.stream()
    .filter(doc -> uniqueContents.add(doc.getText()))
    .collect(Collectors.toList());
```

---

## 6. 答案质量评估与优化

### 6.1 API接口实现

创建`DocumentController.java`，提供完整的文档管理和问答API：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/etl/src/main/java/com/sandy/etl/DocumentController.java
package com.sandy.etl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@Slf4j
public class DocumentController {
    
    @Autowired
    private DocumentService documentService;
    
    @Autowired
    private RagChatService ragChatService;

    /**
     * ⭐ 上传文档
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("收到上传请求: 文件名={}", file.getOriginalFilename());
        try {
            documentService.uploadAndProcessFile(file);
            return ResponseEntity.ok("文件上传并处理成功");
        } catch (IOException e) {
            log.error("上传失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("文件处理失败: " + e.getMessage());
        }
    }

    /**
     * ⭐ 删除文档
     */
    @DeleteMapping("/delete/{fileName}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        log.info("收到删除请求: 文件名={}", fileName);
        try {
            documentService.deleteFile(fileName);
            return ResponseEntity.ok("文件删除成功");
        } catch (RuntimeException e) {
            log.error("删除失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("删除文件失败: " + e.getMessage());
        }
    }

    /**
     * ⭐ RAG问答
     */
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody String question) {
        log.info("收到聊天请求: 问题={}", question);
        try {
            String response = ragChatService.chat(question);
            log.info("聊天响应: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("聊天失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("聊天处理失败: " + e.getMessage());
        }
    }

    /**
     * 列出所有文档
     */
    @GetMapping("/list")
    public ResponseEntity<List<String>> listFiles() {
        log.info("收到文件列表请求");
        try {
            List<String> files = documentService.listFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("获取文件列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(List.of());
        }
    }
}
```

**API说明**：

| 接口 | 方法 | 说明 |
|------|------|------|
| `POST /api/documents/upload` | 上传文档 | 支持PDF、Word、PPT、TXT |
| `DELETE /api/documents/delete/{fileName}` | 删除文档 | 从向量库和物理删除 |
| `POST /api/documents/chat` | RAG问答 | 基于文档回答问题 |
| `GET /api/documents/list` | 文档列表 | 获取所有已上传文档 |

### 6.2 测试RAG系统

**启动应用**：

```bash
# 1. 启动Chroma
docker run -d -p 8000:8000 --name chroma chromadb/chroma

# 2. 启动Ollama
ollama pull qwen2.5

# 3. 启动应用
cd etl
mvn spring-boot:run
```

**上传文档**：

```bash
# 上传PDF
curl -X POST http://localhost:8081/api/documents/upload \
  -F "file=@document.pdf"

# 上传Word
curl -X POST http://localhost:8081/api/documents/upload \
  -F "file=@report.docx"
```

**RAG问答**：

```bash
curl -X POST http://localhost:8081/api/documents/chat \
  -H "Content-Type: text/plain" \
  -d "文档中提到的核心技术是什么？"

# 返回：
# "根据文档内容，核心技术包括：1. 向量检索 2. Embedding模型 3. RAG架构..."
```

### 6.3 评估指标

**准确性指标**：

```java
// 伪代码：评估答案准确性
double accuracy = evaluateAnswer(
    question,        // 问题
    generatedAnswer, // AI生成的答案
    groundTruth      // 标准答案
);
```

**覆盖率指标**：

```
覆盖率 = 能回答的问题数 / 总问题数

示例：
总问题：100个
能回答：85个
覆盖率：85%
```

**响应时间**：

```java
long startTime = System.currentTimeMillis();
String answer = ragChatService.chat(question);
long duration = System.currentTimeMillis() - startTime;

log.info("响应时间: {}ms", duration);
```

### 6.4 优化方案

**优化1：调整Top-K**

```java
// 测试不同Top-K值的效果
SearchRequest request = SearchRequest.builder()
    .query(question)
    .topK(5)  // 试验：3、5、10、20
    .build();
```

**优化2：调整相似度阈值**

```java
SearchRequest request = SearchRequest.builder()
    .query(question)
    .similarityThreshold(0.3)  // 试验：0.1、0.3、0.5、0.7
    .topK(10)
    .build();
```

**优化3：文档质量过滤**

```java
// 只返回高质量文档片段
List<Document> highQualityDocs = documents.stream()
    .filter(doc -> doc.getScore() > 0.5)
    .filter(doc -> doc.getText().length() > 50)
    .collect(Collectors.toList());
```

**优化4：重排序（Reranking）**

```java
// 使用更精确的模型对检索结果重新排序
List<Document> reranked = rerankingModel.rerank(
    question,
    documents
);
```

---

## 💻 示例代码

完整项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/etl](https://github.com/Mark7766/spring-ai-apps/tree/main/etl)

**项目结构**：
```
etl/
├── src/main/java/com/sandy/etl/
│   ├── EtlApplication.java               # 启动类
│   ├── DocumentConfiguration.java        # 分片器配置
│   ├── ChromaCfg.java                    # Chroma配置
│   ├── DocumentService.java              # 文档ETL服务
│   ├── RagChatService.java               # RAG问答服务
│   ├── DocumentController.java           # REST API
│   └── WebController.java                # Web界面
├── src/main/resources/
│   ├── application.yml                   # 配置文件
│   ├── templates/                        # Web模板
│   └── static/                           # 静态资源
├── docs/                                 # 文档存储目录
└── pom.xml
```

**核心文件**：
- **DocumentService.java**：200行完整ETL实现
  - 多格式文档处理
  - 文档分片
  - 向量化存储
- **RagChatService.java**：RAG问答核心逻辑
- **DocumentController.java**：完整的REST API

---

## 🤔 思考题

1. **如何设计文档分片策略以保持语义完整性？**
   
   提示：考虑按段落、按句子、保留重叠等策略，不同类型文档需要不同策略。

2. **RAG系统如何处理时效性问题（如最新政策）？**
   
   提示：定期更新文档、版本管理、文档过期策略。

3. **如何评估RAG系统的答案准确性？**
   
   提示：人工评估、自动化测试集、A/B测试、用户反馈。

---

## 📖 拓展阅读

- [RAG技术论文（Lewis等，2020）](https://arxiv.org/abs/2005.11401)
- [Spring AI ETL文档](https://docs.spring.io/spring-ai/reference/api/etl-pipeline.html)
- [Apache Tika文档](https://tika.apache.org/docs.html)
- [Prompt Engineering指南](https://www.promptingguide.ai/)

---

## ⏭️ 下期预告

恭喜你掌握了RAG技术！🎉 现在你已经能够：
- ✅ 处理多种格式文档
- ✅ 实现完整的ETL Pipeline
- ✅ 构建企业级文档问答系统

但当前的RAG还有局限：
- ❌ 无法理解数据间的关系
- ❌ 无法进行多跳推理
- ❌ 对复杂问题回答不够深入

**下一期我们将学习GraphRAG知识图谱**，让AI理解数据之间的关系网络，实现更智能的推理！

**下期亮点**：
- 🕸️ 知识图谱原理与构建
- 🔗 实体关系抽取
- 🧠 基于图的多跳推理
- 🎯 Neo4j图数据库集成
- 📊 可视化知识网络

敬请期待！

---

**更新日期**：2025年12月2日  
**状态**：✅ 已完成


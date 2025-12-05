# Java开发者的AI破局之路：用Spring AI构建企业级多模态应用

> **关键词**：Java、Spring AI、多模态、Ollama、私有化部署、企业级应用
> 
> **目标读者**：Java后端开发者、技术负责人、希望在现有Java体系中引入AI能力的企业团队

---

## 一、Java开发者的AI困境

**场景一：技术规划会**

> **CTO**：“我们今年的重点是AI赋能，要在客服系统里加入图片识别功能，用户上传截图就能定位问题。”
> **Java团队负责人**：“好的，那我提个招聘需求，我们需要招两位Python工程师，专门负责AI模型服务。”
> **CTO**：“又要招人？我们现有的Java团队不能做吗？”

**场景二：项目评审会**

> **架构师**：“我们设计了一个跨语言调用方案。Java后端通过RPC调用Python写的AI服务。需要维护两套技术栈、两套部署流程、两套监控体系。”
> **运维负责人**： “这...维护成本太高了，而且跨语言调用链路长，出问题排查起来很麻烦。”

这是许多Java团队面临的真实困境：
1.  **技术栈割裂**：为了AI功能，被迫引入Python，导致团队技术栈混乱。
2.  **集成复杂**：跨语言调用、数据同步、环境管理，处处是坑。
3.  **人才壁垒**：感觉AI是Python的专利，Java开发者望而却步。
4.  **安全担忧**：调用外部AI API，公司核心数据（如用户截图、内部文档）需要上传，存在泄露风险。

> 难道Java开发者就只能做AI应用的“外围”工作吗？
> 
> **不！Spring AI的出现，彻底改变了游戏规则。**

---

## 二、Spring AI革命：在Java舒适区玩转AI

Spring AI 是Spring官方推出的AI框架，它将AI能力无缝融入了我们熟悉的Spring Boot生态。

### 核心设计思想

```
┌─────────────────────────────────────────────────────────┐
│                    企业现有Java生态                      │
│                                                         │
│  ┌──────────────────┐       ┌─────────────────────────┐   │
│  │ Spring Boot 应用 │──────>│   Spring AI 统一抽象层  │   │
│  └──────────────────┘       └─────────────────────────┘   │
│                                     │                     │
│         ┌───────────────────────────┴──────────────────┐    │
│         │             │             │                  │    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐       ┌──────────┐│
│  │  Ollama  │  │  OpenAI  │  │  Azure   │ ..... │ 其他模型 ││
│  │ (私有化) │  │ (公有云) │  │ (公有云) │       │ (适配器) ││
│  └──────────┘  └──────────┘  └──────────┘       └──────────┘│
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 四大核心优势

| 维度 | 传统Python方案 | Spring AI方案 |
|------|----------------|---------------|
| **技术栈** | Java + Python | **✅ 纯Java技术栈** |
| **集成成本** | ⚠️ 高，跨语言调用 | **✅ 零成本，原生集成** |
| **数据安全** | 依赖第三方API | **✅ 支持Ollama私有化部署** |
| **开发效率** | 学习曲线陡峭 | **✅ 复用现有Spring知识** |

---

## 三、技术实战：30分钟构建多模态AI应用

我们将基于`chat-ocr`项目，完整展示如何用Spring AI构建一个支持“文本+图像”的多模态聊天应用。

### 3.1 环境准备（5分钟）

**第一步：安装Ollama（本地AI模型运行环境）**

```bash
# Windows系统
# 访问 https://ollama.com/download 下载安装包

# 安装后，下载轻量级多模态模型
# 推荐轻量级模型gemma3:1b（约600MB）
ollama pull gemma3:1b

# 如需更高准确度，可使用gemma3:12b（约7GB，本项目作者使用）
# ollama pull gemma3:12b
```

**第二步：项目依赖（pom.xml）**

```xml
<!-- pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>
    
    <groupId>com.sandy</groupId>
    <artifactId>chat-ocr</artifactId>
    <version>1.0.0-rc1</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- Spring AI Ollama -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
        </dependency>
        
        <!-- JSON处理 -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
        </dependency>
        
        <!-- Lombok简化代码 -->
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

**第三步：配置文件（application.yml）**

```yaml
# application.yml
server:
  port: 8080

spring:
  application:
    name: chat-ocr
  
  # Spring AI Ollama配置
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: gemma3:1b       # 轻量级多模态模型
          temperature: 0.7       # 平衡创造性和准确性
  
  # 文件上传配置
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB

# 聊天存储配置
chat:
  storage-path: ${user.dir}/data   # 数据存储路径
  
  # 场景配置
  scenarios:
    design:
      name: 设计图纸审核
      system-prompt: 你是一位专业的工程设计审查专家...
    medical:
      name: 医疗影像问诊
      system-prompt: 你是一位医学影像分析助手...
    equipment:
      name: 设备故障诊断
      system-prompt: 你是一位设备维修专家...
```

---

### 3.2 核心代码实现

#### **模块1：多模态AI对话服务 (`OcrChatService.java`)**

这是整个应用的核心，负责处理文本和图像，并与AI模型交互。

```java
package com.sandy.chat.ocr.service;

import com.sandy.chat.ocr.config.ScenarioProperties;
import com.sandy.chat.ocr.model.MessageRole;
import com.sandy.chat.ocr.model.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OcrChatService {
    private final ChatModel chatModel;
    private final ScenarioProperties scenarioProperties;
    private final SessionService sessionService;
    private final MessageService messageService;
    private final FileStorageService fileStorageService;
    
    @Autowired
    public OcrChatService(ChatModel chatModel, /*... other services ...*/) {
        this.chatModel = chatModel;
        // ...
    }
    
    public String processChat(String sessionId, String message, 
                             List<MultipartFile> images) throws Exception {
        Session session = sessionService.getSession(sessionId);
        // ... (保存消息和图片)

        String fullMessage = buildMessageWithScenario(session, message);

        String aiResponse;
        if (images != null && !images.isEmpty()) {
            aiResponse = processWithImages(fullMessage, images);
        } else {
            aiResponse = processTextOnly(fullMessage);
        }

        messageService.addMessage(sessionId, MessageRole.ASSISTANT, aiResponse, null);
        return aiResponse;
    }
    
    private String processWithImages(String message, 
                                    List<MultipartFile> images) throws Exception {
        log.info("Processing request with {} image(s) using gemma3:1b", images.size());
        
        // 1. 构建Media列表
        List<Media> mediaList = new ArrayList<>();
        for (MultipartFile image : images) {
            Media media = new Media(
                    MimeTypeUtils.parseMimeType(image.getContentType()),
                    new ByteArrayResource(image.getBytes())
            );
            mediaList.add(media);
        }

        // 2. 构建多模态UserMessage
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(mediaList)
                .build();
        
        // 3. 创建Prompt并调用AI
        Prompt prompt = new Prompt(userMessage);
        ChatResponse response = chatModel.call(prompt);
        
        return response.getResult().getOutput().getText();
    }
    
    private String processTextOnly(String message) {
        // ... (处理纯文本消息)
    }
}
```
**关键点**：`UserMessage.builder().text(...).media(...)` 是实现多模态请求的核心。Spring AI负责将文本和图片数据正确编码并发送给Ollama。

#### **模块2：会话管理服务 (`SessionService.java`)**

负责创建和管理用户会话，为上下文理解提供基础。

```java
package com.sandy.chat.ocr.service;

import com.sandy.chat.ocr.model.Scenario;
import com.sandy.chat.ocr.model.Session;
import com.sandy.chat.ocr.util.JsonFileStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class SessionService {

    private final JsonFileStore jsonFileStore;
    private Path storagePath;
    // ...

    public Session createSession(String name, Scenario scenario) throws IOException {
        String sessionId = "session-" + UUID.randomUUID().toString();
        Session session = Session.builder()
                .id(sessionId)
                .name(name != null ? name : scenario.getDisplayName() + " - " + LocalDateTime.now().toLocalDate())
                .scenario(scenario)
                // ...
                .build();

        // 保存会话元数据到JSON文件
        Path sessionFile = storagePath.resolve(sessionId + ".json");
        jsonFileStore.write(sessionFile, session);

        // 创建会话专属目录
        Path sessionDir = getSessionDirectory(sessionId);
        Files.createDirectories(sessionDir);
        Files.createDirectories(sessionDir.resolve("images"));

        log.info("Created new session: {}", sessionId);
        return session;
    }
    // ...
}
```
**关键点**：使用文件系统进行持久化，简单、可靠，非常适合快速原型和中小型应用。每个会话都有独立的目录，清晰地存储了元数据、消息历史和图片。

#### **模块3：Web接口控制器 (`ChatController.java` & `SessionController.java`)**

提供RESTful API，让前端或其他服务可以与我们的AI应用交互。

```java
package com.sandy.chat.ocr.controller;

import com.sandy.chat.ocr.dto.ChatResponse;
import com.sandy.chat.ocr.service.OcrChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final OcrChatService ocrChatService;
    // ...

    @PostMapping(value = "/chat/{sessionId}", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatResponse chatWithSession(
            @PathVariable String sessionId,
            @RequestParam("message") String message,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        // ...
        String response = ocrChatService.processChat(sessionId, message, images);
        return new ChatResponse(response, true);
    }
}
```
```java
package com.sandy.chat.ocr.controller;

import com.sandy.chat.ocr.model.Session;
import com.sandy.chat.ocr.service.SessionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    // ...

    @PostMapping
    public Session createSession(@RequestBody CreateSessionRequest request) throws IOException {
        return sessionService.createSession(request.getName(), request.getScenario());
    }
}
```
**关键点**：标准的Spring MVC控制器，使用`@PostMapping`和`@RequestParam`处理`multipart/form-data`请求，这是Web应用上传文件的标准方式。

---

## 四、实战演示：从代码到应用

### 场景1：Web界面交互

启动应用后，访问 `http://localhost:8080`。

1.  **选择场景**：首页会展示`application.yml`中配置的各种业务场景。
2.  **进入聊天**：选择一个场景，系统会创建一个新会话。
3.  **多模态输入**：你可以发送文字，也可以点击图片按钮上传一张或多张图片，然后输入问题一起发送。
4.  **查看结果**：AI的回答会显示在聊天窗口中，整个对话历史被完整保存。

### 场景2：API接口测试 (`curl`)

```bash
# 1. 创建一个新会话
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"scenario":"design"}'

# 返回会话ID: {"id":"session-abc-123", ...}

# 2. 在该会话中，上传图片并提问
curl -X POST http://localhost:8080/api/chat/session-abc-123 \
  -F "images=@path/to/your/image.jpg" \
  -F "message=分析这张图片里的内容"
```

**AI返回结果示例**：
```json
{
  "content": "这张图片展示了一个工业设备的控制面板。主要包含以下几个部分：\n1. **显示屏**：位于中央，显示当前运行参数。\n2. **按钮区域**：分布在屏幕下方，有启动、停止、急停等按钮。\n3. **指示灯**：位于顶部，显示电源、运行、报警状态。",
  "success": true
}
```

---

## 五、私有化部署：将AI牢牢掌握在自己手中

### 5.1 硬件要求

| 配置项 | 最低配置 (gemma3:1b) | 推荐配置 (gemma3:12b) |
|--------|----------------------|-----------------------|
| CPU | 4核 | 8核以上 |
| 内存 | 8GB | 16GB |
| 硬盘 | 50GB | 200GB (存储模型和数据) |

### 5.2 Docker部署（生产环境推荐）

```dockerfile
# Dockerfile
FROM openjdk:17-slim

# 安装Ollama
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://ollama.com/install.sh | sh

# 复制应用
COPY target/chat-ocr-1.0.0-rc1.jar /app.jar

# 启动脚本
COPY start.sh /start.sh
RUN chmod +x /start.sh

EXPOSE 8080 11434

CMD ["/start.sh"]
```

```bash
# start.sh
#!/bin/bash
# 后台启动Ollama服务
ollama serve &
# 等待Ollama启动
sleep 5
# 拉取模型（如果本地没有）
ollama pull gemma3:1b
# 启动Java应用
java -jar /app.jar
```

```bash
# 构建并运行Docker容器
docker build -t chat-ocr-app .
docker run -d -p 8080:8080 -v ./data:/app/data chat-ocr-app
```
**关键点**：通过Docker，我们将Ollama和Spring Boot应用打包在一起，实现了AI能力和业务逻辑的一体化交付，极大简化了部署和运维。

---

## 六、企业级扩展：不止于Demo

### 6.1 接入企业知识库 (RAG)

对于专业场景，通用模型可能不够准确。我们可以通过RAG（检索增强生成）模式，让AI参考企业内部文档来回答问题。

```java
// 伪代码
public String queryWithRAG(String question, File image) {
    // 1. 向量化问题，在向量数据库中检索相似的内部文档
    List<Document> contextDocs = vectorStore.similaritySearch(question);
    
    // 2. 构建增强的Prompt
    String context = contextDocs.stream().map(Document::getContent).collect(Collectors.joining("\n"));
    String enhancedPrompt = "请参考以下内部知识：\n" + context + "\n\n根据这些知识和图片，回答问题：" + question;
    
    // 3. 调用多模态AI
    return ocrChatService.processWithImages(enhancedPrompt, image);
}
```

### 6.2 集成现有系统

- **客服系统**：将此应用作为微服务，为客服人员提供智能辅助，分析用户上传的截图。
- **OA/审批流**：用于发票、合同等文件的自动识别和信息提取。
- **MES/WMS**：用于生产现场的质检、扫码等场景。

---

## 七、总结：Java开发者的AI新时代

Spring AI为广大的Java开发者打开了一扇通往AI世界的大门。我们不再需要“绕道”Python，而是可以在自己最熟悉、最擅长的技术栈中，构建安全、可靠、可维护的企业级AI应用。

### 核心收获

1.  **技术自信**：证明了Java完全有能力胜任端到端的AI应用开发。
2.  **架构优势**：实现了AI能力与业务逻辑的完美融合，降低了系统复杂度和维护成本。
3.  **数据安全**：通过私有化部署，确保了企业核心数据100%在控。
4.  **快速落地**：借助Spring Boot的生态优势，可以快速将AI功能产品化。

### 立即行动

克隆项目，启动应用，亲自体验在Java世界中驾驭AI的乐趣吧！

**项目地址**：https://github.com/Mark7766/spring-ai-apps/tree/main/chat-ocr

---

**文章标签**：#Java #SpringAI #多模态 #私有化部署 #企业架构


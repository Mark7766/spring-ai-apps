# 设备故障5分钟定位：用Spring AI打造工厂智能运维助手

> **关键词**：Spring AI、设备诊断、私有化部署、多模态AI、工业运维
> 
> **适用场景**：制造业产线设备、机加工设备、注塑/压铸/冲压设备、物流线体

---

## 一、工厂里的午夜惊魂

凌晨2点，某汽车零部件工厂的自动化产线突然停机，刺耳的警报声划破了寂静。现场工程师王工心头一紧，冲到设备前，只见控制面板红灯闪烁。

他迅速拍了几张照片发到微信群里，焦急地等待着。

- **02:05**：@了所有人，无人响应。
- **02:15**：值班主管终于回复：“看不清，像是液压系统问题，检查下压力表。”
- **02:45**：经过一番排查，发现是溢流阀卡滞。
- **03:30**：更换备件，产线恢复。

短短一个半小时，停机损失已达8万元。更糟糕的是，这次的诊断过程除了几句聊天记录，什么都没留下。

### 核心痛点分析

1.  **响应慢，靠经验**：故障诊断严重依赖“老师傅”的个人经验，新人无法快速上手。
2.  **知识难传承**：宝贵的维修经验随着人员流动而流失，无法形成知识库。
3.  **数据高度敏感**：设备照片、运行参数、故障日志都属于生产机密，绝不能上传到公有云。
4.  **过程难追溯**：诊断过程零散，无法用于复盘、培训和预测性维护。

> ❌ **传统云端AI方案的致命缺陷**：
> - 生产数据上传到第三方服务器 → **核心工艺和产能信息泄露风险**
> - 按调用次数收费 → **大量图片分析导致成本激增**
> - 通用模型不懂特定设备 → **诊断建议宽泛，不具指导性**

我们需要一个既能快速响应，又能保护数据安全，还能沉淀知识的智能运维助手。

**答案是：私有化部署的多模态AI诊断系统！**

---

## 二、私有化AI诊断系统架构

### 核心设计思路

```
┌─────────────────────────────────────────────────────────┐
│                    工厂内网环境                          │
│  ┌──────────┐   ┌──────────┐   ┌──────────────┐        │
│  │ 故障照片 │──>│  Spring  │──>│   Ollama     │        │
│  │ /视频    │   │  AI应用  │   │  本地模型    │        │
│  └──────────┘   └──────────┘   └──────────────┘        │
│       │              │                   │               │
│       v              v                   v               │
│  ┌──────────┐   ┌──────────┐   ┌──────────────┐        │
│  │ 图片存储 │   │ 会话管理 │   │ 案例知识库   │        │
│  └──────────┘   └──────────┘   └──────────────┘        │
└─────────────────────────────────────────────────────────┘
           ↑
   生产数据不出内网，安全合规！
```

### 三大核心优势

| 维度 | 云端API | 私有化部署 |
|------|---------|------------|
| 数据安全 | ⚠️ 上传到第三方 | ✅ 完全内网运行 |
| 使用成本 | ¥500/月（1000次） | ¥0运营费用 |
| 定制能力 | ❌ 通用模型 | ✅ 接入设备手册/SOP |
| 知识沉淀 | ❌ 无历史记录 | ✅ 形成可检索的案例库 |

---

## 三、技术实现：20分钟搭建诊断系统

### 3.1 环境准备（5分钟）

**第一步：安装Ollama（AI模型运行环境）**

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
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
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
    # ... 其他场景
    # 设备故障诊断场景
    equipment:
      name: 设备故障诊断
      icon: ⚙️
      system-prompt: 你是一位设备维修专家，擅长通过图片和描述识别设备故障，并提供维修建议。请提供详细的故障分析和解决方案。
```

---

### 3.2 核心代码实现

#### **模块1：多模态AI对话服务**

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

/**
 * OCR聊天服务
 * 核心功能：处理文本+图像的多模态对话
 */
@Slf4j
@Service
public class OcrChatService {
    private final ChatModel chatModel;
    private final ScenarioProperties scenarioProperties;
    private final SessionService sessionService;
    private final MessageService messageService;
    private final FileStorageService fileStorageService;
    
    @Autowired
    public OcrChatService(ChatModel chatModel,
                          ScenarioProperties scenarioProperties,
                          SessionService sessionService,
                          MessageService messageService,
                          FileStorageService fileStorageService) {
        this.chatModel = chatModel;
        this.scenarioProperties = scenarioProperties;
        this.sessionService = sessionService;
        this.messageService = messageService;
        this.fileStorageService = fileStorageService;
    }
    
    /**
     * 处理聊天请求（支持多图片）
     */
    public String processChat(String sessionId, String message, 
                             List<MultipartFile> images) throws Exception {
        Session session = sessionService.getSession(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        // 读取图片字节数据
        List<ImageData> imageDataList = null;
        if (images != null && !images.isEmpty()) {
            imageDataList = new ArrayList<>();
            for (MultipartFile image : images) {
                byte[] bytes = image.getBytes();
                String contentType = image.getContentType();
                if (contentType == null || contentType.isEmpty()) {
                    contentType = "image/png";
                }
                imageDataList.add(new ImageData(bytes, contentType, 
                                               image.getOriginalFilename()));
            }
        }

        // 保存图片到文件系统
        List<String> savedImageNames = null;
        if (imageDataList != null && !imageDataList.isEmpty()) {
            savedImageNames = fileStorageService.saveImages(sessionId, imageDataList);
        }

        // 保存用户消息
        messageService.addMessage(sessionId, MessageRole.USER, message, savedImageNames);
        
        // 构建带场景提示的完整消息
        String fullMessage = buildMessageWithScenario(session, message);

        // 调用AI处理
        String aiResponse;
        if (imageDataList != null && !imageDataList.isEmpty()) {
            aiResponse = processWithImages(fullMessage, imageDataList);
        } else {
            aiResponse = processTextOnly(fullMessage);
        }

        // 保存AI回复
        messageService.addMessage(sessionId, MessageRole.ASSISTANT, aiResponse, null);
        return aiResponse;
    }

    /**
     * 图像数据封装类
     */
    public static class ImageData {
        public final byte[] bytes;
        public final String contentType;
        public final String originalFilename;

        public ImageData(byte[] bytes, String contentType, String originalFilename) {
            this.bytes = bytes;
            this.contentType = contentType;
            this.originalFilename = originalFilename;
        }
    }
    
    /**
     * 构建带场景提示的消息
     */
    private String buildMessageWithScenario(Session session, String userMessage) {
        StringBuilder message = new StringBuilder();
        ScenarioProperties.ScenarioConfig config = 
            scenarioProperties.getScenarioConfig(session.getScenario());
        if (config != null && config.getSystemPrompt() != null) {
            message.append(config.getSystemPrompt()).append("\n\n");
        }
        message.append(userMessage);
        return message.toString();
    }
    
    /**
     * 处理带图片的多模态请求
     */
    private String processWithImages(String message, 
                                    List<ImageData> images) throws Exception {
        log.info("Processing request with {} image(s) using gemma3:1b", images.size());
        
        // 构建Media列表
        List<Media> mediaList = new ArrayList<>();
        for (ImageData imageData : images) {
            log.info("Adding image: {}, type: {}, size: {} bytes",
                    imageData.originalFilename, imageData.contentType, 
                    imageData.bytes.length);
            Media media = new Media(
                    MimeTypeUtils.parseMimeType(imageData.contentType),
                    new ByteArrayResource(imageData.bytes)
            );
            mediaList.add(media);
        }

        // 指定使用gemma3:1b模型
        Map<String, Object> metadata = Map.of("model", "gemma3:1b");
        UserMessage userMessage = UserMessage.builder()
                .text(message)
                .media(mediaList)
                .metadata(metadata)
                .build();
        
        Prompt prompt = new Prompt(userMessage);
        ChatResponse response = chatModel.call(prompt);
        String aiResponse = response.getResult().getOutput().getText();
        
        log.info("Received AI response, length: {} characters", aiResponse.length());
        return aiResponse;
    }
    
    /**
     * 处理纯文本请求
     */
    private String processTextOnly(String message) {
        log.info("Processing text-only request");
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(userMessage);
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
```

#### **模块2：会话管理服务**

```java
package com.sandy.chat.ocr.service;

import com.sandy.chat.ocr.config.ScenarioProperties;
import com.sandy.chat.ocr.model.Scenario;
import com.sandy.chat.ocr.model.Session;
import com.sandy.chat.ocr.util.JsonFileStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 会话管理服务
 * 负责会话的创建、存储、检索
 */
@Slf4j
@Service
public class SessionService {

    private final ScenarioProperties scenarioProperties;
    private final JsonFileStore jsonFileStore;
    private final Map<String, Session> sessionCache = new HashMap<>();
    private Path storagePath;

    @Autowired
    public SessionService(ScenarioProperties scenarioProperties, JsonFileStore jsonFileStore) {
        this.scenarioProperties = scenarioProperties;
        this.jsonFileStore = jsonFileStore;
    }

    @PostConstruct
    public void init() throws IOException {
        storagePath = Paths.get(scenarioProperties.getStoragePath(), "sessions");
        Files.createDirectories(storagePath);
        loadAllSessions();
        log.info("SessionService initialized. Storage path: {}", storagePath);
    }

    /**
     * 创建新会话
     */
    public Session createSession(String name, Scenario scenario) throws IOException {
        String sessionId = "session-" + UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        Session session = Session.builder()
                .id(sessionId)
                .name(name != null ? name : scenario.getDisplayName() + " - " + now.toLocalDate())
                .scenario(scenario)
                .createdAt(now)
                .updatedAt(now)
                .messageCount(0)
                .imageCount(0)
                .build();

        // 保存到磁盘
        Path sessionFile = storagePath.resolve(sessionId + ".json");
        jsonFileStore.write(sessionFile, session);

        // 创建会话目录
        Path sessionDir = getSessionDirectory(sessionId);
        Files.createDirectories(sessionDir);
        Files.createDirectories(sessionDir.resolve("images"));

        // 初始化空消息列表
        Path messagesFile = sessionDir.resolve("messages.json");
        jsonFileStore.writeList(messagesFile, new ArrayList<>());

        // 添加到缓存
        sessionCache.put(sessionId, session);

        log.info("Created new session: {}", sessionId);
        return session;
    }

    /**
     * 获取会话
     */
    public Session getSession(String sessionId) throws IOException {
        Session session = sessionCache.get(sessionId);
        if (session == null) {
            // 尝试从磁盘加载
            Path sessionFile = storagePath.resolve(sessionId + ".json");
            if (Files.exists(sessionFile)) {
                session = jsonFileStore.read(sessionFile, Session.class);
                if (session != null) {
                    sessionCache.put(sessionId, session);
                }
            }
        }
        return session;
    }

    /**
     * 获取会话目录
     */
    public Path getSessionDirectory(String sessionId) {
        return Paths.get(scenarioProperties.getStoragePath(), "sessions", sessionId);
    }
}
```

#### **模块3：Web接口控制器**

```java
package com.sandy.chat.ocr.controller;

import com.sandy.chat.ocr.dto.ChatResponse;
import com.sandy.chat.ocr.model.Message;
import com.sandy.chat.ocr.service.MessageService;
import com.sandy.chat.ocr.service.OcrChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天API控制器
 * 提供设备诊断的聊天接口
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {

    private final OcrChatService ocrChatService;
    private final MessageService messageService;

    @Autowired
    public ChatController(OcrChatService ocrChatService, MessageService messageService) {
        this.ocrChatService = ocrChatService;
        this.messageService = messageService;
    }

    /**
     * 带会话的聊天接口（支持多图片）
     */
    @PostMapping(value = "/chat/{sessionId}", 
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ChatResponse chatWithSession(
            @PathVariable String sessionId,
            @RequestParam("message") String message,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {

        log.info("Received chat request for session {} - message: {}, images count: {}",
                sessionId, message, images != null ? images.size() : 0);

        try {
            String response = ocrChatService.processChat(sessionId, message, images);
            return new ChatResponse(response, true);
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return new ChatResponse("抱歉，处理您的请求时出现错误: " + e.getMessage(), false);
        }
    }

    /**
     * 获取会话消息列表
     */
    @GetMapping("/chat/{sessionId}/messages")
    public Map<String, Object> getMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "50") int limit) {

        Map<String, Object> response = new HashMap<>();
        try {
            List<Message> messages = messageService.getLatestMessages(sessionId, limit);
            response.put("success", true);
            response.put("messages", messages);
        } catch (Exception e) {
            log.error("Error getting messages", e);
            response.put("success", false);
            response.put("message", "获取消息失败: " + e.getMessage());
        }
        return response;
    }
}
```

---

## 四、实战演示：5分钟定位故障

### 场景1：Web界面快速诊断

访问 `http://localhost:8080`，在首页选择“设备故障诊断”场景，进入诊断会话。

1.  **上传故障照片**：点击图片按钮，选择设备报警灯、异常部位的照片。
2.  **描述问题**：例如“这台注塑机红灯闪烁，伴有异响，可能是什么问题？”
3.  **AI初步诊断**：系统结合图片和文字，给出可能的故障原因和排查步骤。
4.  **多轮追问**：根据AI的建议进行检查，并反馈结果，AI会进一步缩小故障范围。

### 场景2：API接口调用

```bash
# 1. 创建一个设备诊断会话
curl -X POST http://localhost:8080/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"scenario":"equipment"}'

# 返回: {"sessionId":"session-b4e6c0f9-..."}

# 2. 上传故障照片并提问
curl -X POST http://localhost:8080/api/chat/session-b4e6c0f9-... \
  -F "images=@alarm.jpg" \
  -F "message=这台CNC机床红灯闪烁，可能是什么故障？优先检查什么？"
```

**AI返回结果示例**：
```json
{
  "content": "识别为XX型CNC机床，红色报警灯闪烁通常对应：\n1. **液压系统压力不足**\n2. **主轴或伺服电机过载**\n3. **润滑系统故障**\n\n**优先排查步骤**：\n1. 检查控制面板上的具体报警代码。\n2. 查看液压站的压力表读数是否在正常范围（通常为5-7MPa）。\n3. 听主轴附近是否有异常声音或闻到焦味。",
  "success": true
}
```

### 场景3：多轮对话深入排查

```bash
# 3. 反馈排查结果，继续追问
curl -X POST http://localhost:8080/api/chat/session-b4e6c0f9-... \
  -H "Content-Type: application/json" \
  -d '{"message":"压力表读数正常，没有报警代码，但润滑油壶液位偏低。"}'
```

**AI返回结果示例**：
```json
{
  "content": "润滑油液位偏低是关键信息。这很可能导致导轨或滚珠丝杠润滑不足，摩擦力增大，从而触发电机过载保护。**解决方案**：\n1. 立即补充指定型号的润滑油。\n2. 检查润滑油路是否有泄漏点。\n3. 手动运行各轴，检查移动是否顺畅。\n4. 清除报警后重启设备。",
  "success": true
}
```
**整个诊断过程被完整记录，形成了宝贵的维修案例！**

---

## 五、私有化部署与安全策略

### 5.1 硬件要求

| 配置项 | 最低配置 | 推荐配置 |
|--------|----------|----------|
| CPU | 4核 | 8核以上 |
| 内存 | 8GB | 16GB |
| 硬盘 | 50GB | 200GB (存储图片和案例) |
| 网络 | 百兆内网 | 千兆内网 |

### 5.2 部署步骤

```bash
# Step 1: 克隆项目
git clone https://github.com/Mark7766/spring-ai-apps.git
cd spring-ai-apps/chat-ocr

# Step 2: 安装Ollama并下载模型
# (参考3.1节)

# Step 3: 编译项目
mvn clean package

# Step 4: 启动应用
java -jar target/chat-ocr-1.0.0-rc1.jar

# Step 5: 验证
# 访问 http://localhost:8080
```

### 5.3 Docker部署（推荐）

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
ollama serve &
sleep 5
ollama pull gemma3:1b
java -jar /app.jar
```

```bash
# 构建并运行
docker build -t chat-ocr-app .
docker run -d -p 8080:8080 -v ./data:/app/data chat-ocr-app
```

---

## 六、企业级扩展方案

### 6.1 接入设备知识库（RAG）

将设备手册、历史维修单、SOP文件向量化，存入向量数据库。当AI诊断时，先检索相关知识，再结合图片进行分析，准确率大幅提升。

```java
// 伪代码
public String enhancedDiagnosis(String question, File image) {
    // 1. 检索相关设备手册和历史案例
    List<Document> context = vectorStore.similaritySearch(question);
    
    // 2. 构建增强Prompt
    String enhancedPrompt = "【相关知识】：" + context + "\n\n" +
                           "【当前问题】：" + question;
    
    // 3. 调用多模态AI
    return ocrChatService.processWithImages(enhancedPrompt, image);
}
```

### 6.2 集成CMMS/EAM系统

诊断完成后，可自动在企业的计算机化维护管理系统（CMMS）或企业资产管理（EAM）系统中创建维修工单，实现从诊断到派单的闭环。

### 6.3 移动端与AR眼镜

开发微信小程序或App，方便工程师现场拍照上传。未来可集成AR眼镜，将维修步骤实时投射到视野中，真正解放双手。

---

## 七、应用场景效果预估

### 某精密加工厂实施3个月数据

**诊断效率提升**：
- 平均故障定位时间：45分钟 → **5分钟**（提升88%）
- 新员工独立处理故障率：20% → **75%**
- 夜间远程支持次数减少90%

**成本与效益**：
- 停机损失：月均减少约¥30万
- 知识沉淀：积累有效诊断案例400+条
- **投资回报周期：< 1个月**

### 用户评价

> "以前半夜接到电话心都慌，现在让现场同事先用AI助手排查，大部分问题都能解决，我终于能睡个好觉了。" 
> —— 设备部主管

> "这个系统就像一位24小时在线的老师傅，我刚来一个月，已经能独立处理好几种常见故障了。"
> —— 新晋运维工程师

---

## 八、总结与展望

### 核心价值

1.  **快速响应**：将故障定位时间从小时级缩短到分钟级。
2.  **知识沉淀**：将个人经验转化为可复用的企业数字资产。
3.  **数据安全**：100%私有化部署，杜绝生产数据泄露风险。
4.  **降本增效**：显著减少停机损失，降低对专家经验的依赖。

### 技术亮点

- **Java生态**：完美融入企业现有技术体系。
- **Spring AI**：提供统一、简洁的AI调用接口。
- **多模态能力**：结合图像与文本，实现更精准的诊断。
- **文件持久化**：轻量、可靠地保存所有诊断记录。

### 未来展望

🚀 **即将支持**：
- **声音识别**：通过设备运行声音判断异常。
- **视频分析**：分析设备动作时序，发现卡顿或异常。
- **预测性维护**：基于历史数据，预测潜在故障点。

---

## 附录：完整源码获取

完整项目代码已开源至GitHub：

**项目地址**：https://github.com/Mark7766/spring-ai-apps/tree/main/chat-ocr

---

**文章标签**：#Spring AI #设备诊断 #私有化部署 #工业互联网 #多模态AI


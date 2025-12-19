# 第11期：MCP协议服务 - 用Java构建可复用的AI工具生态

## 📌 本期概述

**核心问题：如何让AI工具模块化、可复用、易分享？**

Model Context Protocol (MCP) 是一个开放的AI工具通信协议，它让你的AI工具可以像微服务一样独立部署、跨应用复用。本期将深入讲解如何**使用Java语言**通过Spring AI框架实现MCP Server和Client，构建时间工具服务，让AI能够远程调用你的工具。

## 🎯 学习目标

完成本期学习后，你将能够：
- ✅ 理解MCP协议的核心概念和工作原理
- ✅ **用Java + Spring AI框架实现MCP Server服务**
- ✅ **用Java开发MCP Client客户端应用**
- ✅ **通过Java代码实现工具的远程调用和编排**
- ✅ 掌握MCP服务的部署和集成
- ✅ 构建可复用的AI工具生态

## 📚 内容大纲

### 1. MCP协议详解

### 2. MCP Server实现

### 3. MCP Client开发

### 4. 工具远程调用实战

### 5. MCP vs Function Calling对比

### 6. 生产环境部署

---

## 1. MCP协议详解

### 1.1 什么是MCP？

**MCP (Model Context Protocol)** 是Anthropic公司推出的开放标准，用于AI应用和外部工具之间的通信。

```
传统Function Calling：
AI应用 → 工具函数（本地代码）
限制：工具绑定在应用内，无法复用

MCP架构：
AI应用 (Client) → HTTP/SSE → 工具服务 (Server)
优势：工具独立部署，跨应用复用
```

### 1.2 MCP vs Function Calling

| 对比维度 | Function Calling | MCP |
|---------|-----------------|-----|
| **工具位置** | 本地代码 | 远程服务 |
| **复用性** | 单应用 | 跨应用复用 |
| **部署** | 与应用绑定 | 独立部署 |
| **升级** | 需重启应用 | 独立升级 |
| **共享** | 代码级共享 | 服务级共享 |
| **通信** | 直接调用 | HTTP/SSE |

### 1.3 MCP工作流程

```
【MCP Server端】
1. 注册工具：@Tool注解定义工具
2. 启动服务：暴露HTTP SSE端点
3. 等待调用：监听/mcp/messages

【MCP Client端】
1. 发现工具：连接Server，获取工具列表
2. 用户提问：AI识别需要调用的工具
3. 远程调用：通过HTTP调用Server工具
4. 获取结果：返回工具执行结果
5. 生成回复：基于结果生成自然语言
```

**完整流程图**：

```
用户："现在几点了？"
    ↓
【Client: AI分析】
  识别：需要调用getCurrentDateTime工具
    ↓
【Client → Server】
  HTTP POST /mcp/messages
  { "tool": "getCurrentDateTime", "params": {} }
    ↓
【Server: 执行工具】
  getCurrentDateTime() → "2025-12-03T14:30:00"
    ↓
【Server → Client】
  SSE Response: { "result": "2025-12-03T14:30:00" }
    ↓
【Client: AI生成回复】
  "现在是2025年12月3日下午2点30分。"
```

### 1.4 MCP的应用场景

**场景1：企业工具市场**
```
公司内部部署多个MCP Server：
- 财务数据查询服务
- CRM客户管理服务
- 文档检索服务

所有AI应用都可以调用这些工具
```

**场景2：第三方工具集成**
```
使用社区提供的MCP Server：
- 天气查询服务
- 股票数据服务
- 翻译服务

无需自己开发，直接集成
```

**场景3：微服务架构**
```
将AI能力拆分成独立服务：
- 文本摘要服务
- 情感分析服务
- 实体识别服务

按需调用，弹性伸缩
```

---

## 2. MCP Server实现（Java版本）

现在开始用Java构建MCP Server服务！

项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/mcp-ollama/mcp-ollama-server](https://github.com/Mark7766/spring-ai-apps/tree/main/mcp-ollama/mcp-ollama-server)

### 2.1 Server依赖配置

```xml
<!-- 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/mcp-ollama/mcp-ollama-server/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.4</version>
    </parent>
    
    <groupId>com.sandy</groupId>
    <artifactId>mcp-ollama-server</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
    </properties>
    
    <dependencies>
        <!-- ⭐ Spring AI MCP Server核心依赖 -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
        </dependency>
        
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
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

**核心依赖**：
- `spring-ai-starter-mcp-server-webmvc`：MCP Server自动配置和SSE支持

### 2.2 Server配置

```yaml
# 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/mcp-ollama/mcp-ollama-server/src/main/resources/application.yml
server:
  port: 8091  # MCP Server端口
  tomcat:
    async-timeout: 180000  # 异步超时3分钟

spring:
  application:
    name: "mcp-ollama-server"
  
  servlet:
    async:
      timeout: 180000  # Spring异步超时
  
  ai:
    mcp:
      server:
        enabled: true  # ⭐ 启用MCP Server
        name: webmvc-mcp-server  # Server名称
        version: 1.0.0  # Server版本
        type: SYNC  # 同步模式
        sse-message-endpoint: /mcp/messages  # ⭐ SSE端点
```

**配置说明**：
- `sse-message-endpoint`：Client调用工具的HTTP端点
- `type: SYNC`：同步调用模式
- `async-timeout`：长时间运行的工具需要足够的超时

### 2.3 定义MCP工具

创建`McpServerTool.java`：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/mcp-ollama/mcp-ollama-server/src/main/java/com/sandy/mcp/ollama/server/McpServerTool.java
package com.sandy.mcp.ollama.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
@Slf4j
public class McpServerTool {

    /**
     * ⭐ MCP工具1：获取当前时间
     */
    @Tool(
        name = "getCurrentDateTime",
        description = "查询用户所在时区的当前时间"
    )
    String getCurrentDateTime() {
        log.info("getCurrentDateTime: {}", new Date());
        
        // 获取用户时区的当前时间
        String currentDateTime = LocalDateTime.now()
            .atZone(LocaleContextHolder.getTimeZone().toZoneId())
            .toString();
        
        return currentDateTime;
    }

    /**
     * ⭐ MCP工具2：设置闹钟
     */
    @Tool(
        name = "setAlarm",
        description = "将用户闹钟设置为给定的时间，时间格式为 ISO-8601"
    )
    public void setAlarm(String time) {
        log.info("setAlarm: {}", time);
        
        // 解析时间
        LocalDateTime alarmTime = LocalDateTime.parse(
            time, 
            DateTimeFormatter.ISO_DATE_TIME
        );
        
        log.info("Alarm set for {}", alarmTime);
        
        // 实际应用中，这里会调用系统API或存储到数据库
    }
}
```

**代码解析**：

1. **@Component注解**：
   ```java
   @Component
   public class McpServerTool { ... }
   ```
   Spring自动扫描并注册为Bean，MCP Server会自动发现。

2. **@Tool注解**：
   ```java
   @Tool(name = "getCurrentDateTime", description = "...")
   ```
   - `name`：工具唯一标识
   - `description`：工具功能描述（Client端AI识别用）

3. **返回值**：
   工具执行结果会通过SSE返回给Client。

### 2.4 启动MCP Server

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/mcp-ollama/mcp-ollama-server/src/main/java/com/sandy/mcp/ollama/server/McpServerApplication.java
package com.sandy.mcp.ollama.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
```

**启动Server**：

```bash
cd mcp-ollama-server
mvn spring-boot:run
```

**验证Server**：

访问：`http://localhost:8091/mcp/messages`

SSE端点会保持连接，等待Client调用。

---

## 3. MCP Client开发（Java版本）

现在用Java开发Client应用，调用MCP Server的工具！

项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/mcp-ollama/mcp-ollama-client](https://github.com/Mark7766/spring-ai-apps/tree/main/mcp-ollama/mcp-ollama-client)

### 3.1 Client依赖配置

```xml
<!-- 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/mcp-ollama/mcp-ollama-client/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.4</version>
    </parent>
    
    <groupId>com.sandy</groupId>
    <artifactId>mcp-ollama-client</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    
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
        
        <!-- ⭐ Spring AI MCP Client -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-mcp-client</artifactId>
        </dependency>
        
        <!-- ⭐ Spring AI Ollama（本地LLM） -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-ollama</artifactId>
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

**核心依赖**：
- `spring-ai-starter-mcp-client`：MCP Client SDK
- `spring-ai-starter-model-ollama`：本地LLM（用于理解用户意图）

### 3.2 Client配置

```yaml
# 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/mcp-ollama/mcp-ollama-client/src/main/resources/application.yml
server:
  port: 8081  # Client应用端口

spring:
  application:
    name: "mcp-ollama-client"
  
  ai:
    ollama:
      base-url: "http://localhost:11434"
      embedding:
        enabled: true
        model: qwen2.5
      chat:
        enabled: true
        model: qwen2.5  # 本地LLM
```

### 3.3 创建MCP Server代理

创建`McpServerProxy.java`，封装远程工具调用：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/mcp-ollama/mcp-ollama-client/src/main/java/com/sandy/mcp/ollama/client/McpServerProxy.java
package com.sandy.mcp.ollama.client;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class McpServerProxy {

    /**
     * ⭐ 代理工具1：获取当前时间（远程调用）
     */
    @Tool(description = "查询用户所在时区的当前时间")
    String getCurrentDateTime() {
        log.info("getCurrentDateTime: {}", new Date());
        
        // ⭐ Step 1: 构造MCP调用请求
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "getCurrentDateTime",  // 工具名称
            new HashMap<>()        // 参数（空）
        );
        
        // ⭐ Step 2: 创建MCP Client连接
        McpSyncClient client = mcpSyncClient();
        
        // ⭐ Step 3: 调用远程工具
        McpSchema.CallToolResult result = client.callTool(request);
        log.info("Result: {}", JsonParser.toJson(result));
        
        // 关闭连接
        client.close();
        
        // ⭐ Step 4: 提取结果
        McpSchema.TextContent content = (McpSchema.TextContent) result.content().get(0);
        return content.text();
    }

    /**
     * ⭐ 代理工具2：设置闹钟（远程调用）
     */
    @Tool(description = "将用户闹钟设置为给定的时间，时间格式为 ISO-8601")
    public void setAlarm(String time) {
        log.info("setAlarm: {}", time);
        
        // ⭐ 构造参数
        Map<String, Object> params = new HashMap<>();
        params.put("time", time);
        
        // ⭐ 构造请求
        McpSchema.CallToolRequest request = new McpSchema.CallToolRequest(
            "setAlarm",
            params
        );
        
        // ⭐ 调用远程工具
        McpSyncClient client = mcpSyncClient();
        McpSchema.CallToolResult result = client.callTool(request);
        log.info("setAlarm result: {}", JsonParser.toJson(result));
        
        client.close();
    }

    /**
     * ⭐ 创建MCP Client连接
     */
    private McpSyncClient mcpSyncClient() {
        // ⭐ Step 1: 创建HTTP SSE传输
        HttpClientSseClientTransport transport = new HttpClientSseClientTransport(
            "http://localhost:8091"  // MCP Server地址
        );
        
        // ⭐ Step 2: 构建同步Client
        McpSyncClient mcpSyncClient = io.modelcontextprotocol.client.McpClient.sync(transport)
            .build();
        
        // ⭐ Step 3: 获取Server工具列表
        McpSchema.ListToolsResult listToolsResult = mcpSyncClient.listTools();
        log.info("Available tools: {}", JsonParser.toJson(listToolsResult));
        
        return mcpSyncClient;
    }
}
```

**代码核心解析**：

1. **@Tool注解**：
   ```java
   @Tool(description = "查询用户所在时区的当前时间")
   ```
   Client端的AI会根据这个描述识别何时调用此工具。

2. **MCP调用流程**：
   ```java
   // 创建请求
   CallToolRequest request = new CallToolRequest("toolName", params);
   // 创建Client
   McpSyncClient client = mcpSyncClient();
   // 调用远程工具
   CallToolResult result = client.callTool(request);
   // 提取结果
   TextContent content = (TextContent) result.content().get(0);
   ```

3. **HTTP SSE传输**：
   ```java
   HttpClientSseClientTransport transport = new HttpClientSseClientTransport(
       "http://localhost:8091"
   );
   ```
   通过SSE（Server-Sent Events）保持长连接。

---

## 4. 工具远程调用实战（Java实现）

### 4.1 创建Java Client控制器

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/mcp-ollama/mcp-ollama-client/src/main/java/com/sandy/mcp/ollama/client/ClientController.java
package com.sandy.mcp.ollama.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.util.json.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class ClientController {
    
    @Autowired
    private ChatModel chatModel;

    /**
     * ⭐ MCP工具调用接口
     */
    @GetMapping("/ask")
    public String ask(@RequestParam("text") String text) throws Exception {
        log.info("ask: {}", text);
        
        // ⭐ 创建ChatClient并注册MCP代理工具
        ChatResponse response = ChatClient.builder(chatModel)
            .build()
            .prompt()
            .system("You are a helpful assistant.")
            .user(text)
            .tools(new McpServerProxy())  // ⭐ 注册MCP代理
            .call()
            .chatResponse();
        
        log.info("response: {}", JsonParser.toJson(response));
        
        assert response != null;
        return response.getResult().getOutput().getText();
    }
}
```

**工作流程**：

```
用户："现在几点了？"
    ↓
【Client AI分析】
  识别意图：查询时间
  选择工具：getCurrentDateTime
    ↓
【调用MCP代理】
  mcpServerProxy.getCurrentDateTime()
    ↓
【MCP代理 → Server】
  HTTP POST /mcp/messages
  CallToolRequest("getCurrentDateTime", {})
    ↓
【Server执行】
  mcpServerTool.getCurrentDateTime()
  返回："2025-12-03T14:30:00"
    ↓
【Server → MCP代理】
  SSE Response: "2025-12-03T14:30:00"
    ↓
【Client AI生成】
  "现在是2025年12月3日下午2点30分。"
```

### 4.2 测试MCP系统

**启动Server**：
```bash
cd mcp-ollama-server
mvn spring-boot:run
# 端口：8091
```

**启动Client**：
```bash
cd mcp-ollama-client
mvn spring-boot:run
# 端口：8081
```

**测试调用**：

```bash
# 测试1：查询时间
curl "http://localhost:8081/ask?text=现在几点了？"

# AI会调用getCurrentDateTime工具
# 返回："现在是2025年12月3日下午2点30分。"

# 测试2：设置闹钟
curl "http://localhost:8081/ask?text=帮我设置明天早上8点的闹钟"

# AI会调用setAlarm工具
# 返回："好的，已为您设置明天早上8点的闹钟。"
```

**日志输出**：

Server端：
```
McpServerTool - getCurrentDateTime: Thu Dec 03 14:30:00 CST 2025
McpServerTool - Alarm set for 2025-12-04T08:00:00
```

Client端：
```
McpServerProxy - getCurrentDateTime: Thu Dec 03 14:30:00 CST 2025
McpServerProxy - Available tools: [getCurrentDateTime, setAlarm]
McpServerProxy - Result: {"content":[{"type":"text","text":"2025-12-03T14:30:00"}]}
```

---

## 5. MCP vs Function Calling对比

### 5.1 架构对比

**Function Calling（本地工具）**：
```
┌─────────────────────────┐
│     AI Application      │
│  ┌──────────────────┐   │
│  │   ChatModel      │   │
│  └────────┬─────────┘   │
│           ↓             │
│  ┌──────────────────┐   │
│  │  Local Tools     │   │
│  │  - getCurrentTime│   │
│  │  - setAlarm      │   │
│  └──────────────────┘   │
└─────────────────────────┘
```

**MCP架构（远程工具）**：
```
┌──────────────────┐         ┌──────────────────┐
│   AI Application │         │   MCP Server     │
│   (Client)       │         │                  │
│  ┌────────────┐  │         │  ┌────────────┐  │
│  │ ChatModel  │  │         │  │ MCP Tools  │  │
│  └─────┬──────┘  │         │  │ - getTime  │  │
│        ↓         │         │  │ - setAlarm │  │
│  ┌────────────┐  │  HTTP   │  └────────────┘  │
│  │ MCP Proxy  │──┼────→────┤                  │
│  └────────────┘  │   SSE   │                  │
└──────────────────┘         └──────────────────┘
```

### 5.2 选型决策

| 场景 | 推荐方案 | 原因 |
|------|---------|------|
| 简单工具，单应用 | Function Calling | 简单直接 |
| 工具需要跨应用复用 | MCP | 避免重复开发 |
| 工具需要独立升级 | MCP | 不影响主应用 |
| 第三方工具集成 | MCP | 解耦依赖 |
| 微服务架构 | MCP | 符合微服务理念 |
| 性能要求极高 | Function Calling | 避免网络开销 |

### 5.3 混合使用

```java
@GetMapping("/ask")
public String ask(@RequestParam("text") String text) {
    ChatResponse response = ChatClient.builder(chatModel)
        .build()
        .prompt()
        .user(text)
        .tools(
            new LocalTools(),      // ⭐ 本地Function Calling工具
            new McpServerProxy()   // ⭐ MCP远程工具
        )
        .call()
        .chatResponse();
    
    return response.getResult().getOutput().getText();
}
```

AI会根据需要自动选择合适的工具（本地或远程）。

---

## 6. 生产环境部署

### 6.1 MCP Server独立部署

**方式1：Docker部署**

```dockerfile
# Dockerfile for MCP Server
FROM openjdk:17-slim
COPY target/mcp-ollama-server.jar /app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# 构建镜像
docker build -t mcp-server:1.0 .

# 运行容器
docker run -d -p 8091:8091 --name mcp-server mcp-server:1.0
```

**方式2：Kubernetes部署**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mcp-server
spec:
  replicas: 3  # 3个副本
  selector:
    matchLabels:
      app: mcp-server
  template:
    metadata:
      labels:
        app: mcp-server
    spec:
      containers:
      - name: mcp-server
        image: mcp-server:1.0
        ports:
        - containerPort: 8091
---
apiVersion: v1
kind: Service
metadata:
  name: mcp-server-service
spec:
  selector:
    app: mcp-server
  ports:
  - port: 80
    targetPort: 8091
  type: LoadBalancer
```

### 6.2 负载均衡

**Nginx配置**：

```nginx
upstream mcp_servers {
    server mcp-server1:8091;
    server mcp-server2:8091;
    server mcp-server3:8091;
}

server {
    listen 80;
    location /mcp/ {
        proxy_pass http://mcp_servers;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_buffering off;
        proxy_cache off;
    }
}
```

### 6.3 监控与日志

**添加监控依赖**：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**配置监控端点**：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  metrics:
    tags:
      application: mcp-server
```

访问：`http://localhost:8091/actuator/health`

---

## 💻 示例代码

完整项目代码：
- **MCP Server**: [https://github.com/Mark7766/spring-ai-apps/tree/main/mcp-ollama/mcp-ollama-server](https://github.com/Mark7766/spring-ai-apps/tree/main/mcp-ollama/mcp-ollama-server)
- **MCP Client**: [https://github.com/Mark7766/spring-ai-apps/tree/main/mcp-ollama/mcp-ollama-client](https://github.com/Mark7766/spring-ai-apps/tree/main/mcp-ollama/mcp-ollama-client)

**项目结构**：
```
mcp-ollama/
├── mcp-ollama-server/
│   ├── src/main/java/com/sandy/mcp/ollama/server/
│   │   ├── McpServerApplication.java      # 启动类
│   │   ├── McpServerTool.java              # MCP工具定义
│   │   └── ToolBeanConfig.java             # 配置类
│   ├── src/main/resources/
│   │   └── application.yml                 # Server配置
│   └── pom.xml
├── mcp-ollama-client/
│   ├── src/main/java/com/sandy/mcp/ollama/client/
│   │   ├── McpClientApplication.java       # 启动类
│   │   ├── McpServerProxy.java             # MCP代理
│   │   └── ClientController.java           # API控制器
│   ├── src/main/resources/
│   │   ├── application.yml                 # Client配置
│   │   └── static/chat.html                # 前端界面
│   └── pom.xml
└── README.md
```

---

## 🤔 思考题

1. **MCP协议与传统的REST API有什么本质区别？**
   
   提示：考虑工具发现、参数类型、AI集成等方面。

2. **如何实现MCP服务的版本管理和平滑升级？**
   
   提示：可以通过URL版本号、Header版本标识、服务注册中心等方案。

3. **多个MCP Server如何协同工作？**
   
   提示：Client可以连接多个Server，AI根据工具描述选择合适的Server。

---

## 📖 拓展阅读

- [Model Context Protocol官方规范](https://modelcontextprotocol.io/)
- [Spring AI MCP文档](https://docs.spring.io/spring-ai/reference/api/mcp.html)
- [Anthropic MCP介绍](https://www.anthropic.com/news/model-context-protocol)
- [微服务架构设计模式](https://microservices.io/patterns/)

---

## ⏭️ 下期预告

恭喜你掌握了用Java开发MCP协议！🎉 现在你可以用Java构建可复用的AI工具生态了。

**高级篇到此结束！** 下一期进入**实战篇**，我们将学习**智能Agent实战**，用Java构建一个能自主规划、自动执行任务的智能代理！

**下期亮点**：
- 🤖 Agent智能体原理与架构（Java实现）
- 🧠 ReAct思维链Java实现
- 🔄 任务规划与执行循环（Java编码）
- 🛠️ 多工具协同调用（Java集成）
- 📊 Agent状态管理（Java方案）
- 🎯 实战：用Java开发自动化内容编辑助手

从单一工具调用到智能Agent，让Java生态中的AI真正成为你的自动化助手！

敬请期待！

---

**更新日期**：2025年12月3日  
**状态**：✅ 已完成


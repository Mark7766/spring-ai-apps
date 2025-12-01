# 第8期：Function Calling工具调用 - 让AI成为你的智能助手

## 📌 本期概述

**核心问题：如何让AI自主决策并调用你的工具和API？**

传统的AI只能回答问题，无法主动执行操作。Function Calling技术让AI能够识别用户意图，自主选择并调用合适的工具函数，真正成为能干活的智能助手。本期将深入讲解Spring AI的Function Calling机制，实现时间查询、闹钟设置等实用工具集成。

## 🎯 学习目标

完成本期学习后，你将能够：
- ✅ 理解Function Calling的工作原理和应用场景
- ✅ 掌握Spring AI的@Tool注解使用方法
- ✅ 实现自定义工具函数的注册和调用
- ✅ 处理函数参数的自动提取和验证
- ✅ 构建多工具协作的智能应用
- ✅ 掌握错误处理和安全防护策略

## 📚 内容大纲

### 1. Function Calling技术原理

### 2. Spring AI的@Tool注解机制

### 3. 时间工具实战

### 4. 参数提取与验证

### 5. 多函数调用编排

### 6. 安全防护与最佳实践

---

## 1. Function Calling技术原理

### 1.1 什么是Function Calling？

**Function Calling（函数调用）** 是让AI能够识别用户意图，并自动调用预定义函数的技术。

**传统AI对话**：
```
用户："现在几点了？"
AI："抱歉，我无法获取实时信息。我的知识截止到2023年..."
```

**使用Function Calling**：
```
用户："现在几点了？"
AI（内部）：
  1. 识别意图：查询当前时间
  2. 调用函数：getCurrentDateTime()
  3. 获取结果：2025-12-03T14:30:00
  4. 生成回复
AI："现在是2025年12月3日下午2点30分。"
```

### 1.2 工作流程

```
用户输入："帮我设置明天早上8点的闹钟"
    ↓
【Step 1】AI理解意图
  意图：设置闹钟
  参数：时间=明天早上8点
    ↓
【Step 2】AI选择函数
  选择：setAlarm(time)
  提取参数：time="2025-12-04T08:00:00"
    ↓
【Step 3】Spring AI执行函数
  调用：dateTimeTools.setAlarm("2025-12-04T08:00:00")
  返回："Alarm set for 2025-12-04T08:00:00"
    ↓
【Step 4】AI生成自然语言回复
  "好的，已为您设置明天早上8点的闹钟。"
```

### 1.3 Function Calling vs 传统方式

| 对比维度 | 传统方式 | Function Calling |
|---------|---------|-----------------|
| **意图识别** | 手动正则/NLU | AI自动理解 |
| **参数提取** | 手动解析 | AI自动提取 |
| **函数选择** | if-else硬编码 | AI动态决策 |
| **扩展性** | 添加功能需改代码 | 只需添加@Tool |
| **灵活性** | 固定模式 | 自然语言交互 |

**适用场景**：
- ✅ 智能客服（查询订单、退款等）
- ✅ 智能助手（设置提醒、查天气等）
- ✅ 数据查询（查数据库、调API等）
- ✅ 自动化任务（发邮件、创建工单等）

---

## 2. Spring AI的@Tool注解机制

### 2.1 @Tool注解原理

Spring AI提供了`@Tool`注解，让你可以轻松将Java方法暴露给AI调用。

**核心机制**：

```java
@Tool(description = "工具功能描述")
public String myTool(String param) {
    // 你的业务逻辑
    return "执行结果";
}
```

**Spring AI做了什么**：
1. 扫描所有`@Tool`注解的方法
2. 提取方法签名和description
3. 将函数信息发送给LLM
4. LLM决定调用哪个函数
5. Spring AI执行函数并返回结果

### 2.2 函数描述的重要性

**description是AI选择函数的依据！**

❌ **糟糕的描述**：
```java
@Tool(description = "get time")
String getCurrentDateTime() { ... }
```

✅ **好的描述**：
```java
@Tool(description = "查询用户所在时区的当前时间")
String getCurrentDateTime() { ... }
```

**描述最佳实践**：
- 🎯 清晰说明函数的功能
- 📝 用中文描述（如果用户用中文）
- 🔍 包含关键词（时间、查询、设置等）
- ⚠️ 说明限制和前提条件

---

## 3. 时间工具实战

现在开始构建一个支持Function Calling的智能助手！

项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/tools-ollama](https://github.com/Mark7766/spring-ai-apps/tree/main/tools-ollama)

### 3.1 项目依赖配置

```xml
<!-- 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/tools-ollama/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.4</version>
    </parent>
    
    <groupId>com.sandy</groupId>
    <artifactId>tools-ollama</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    
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
        
        <!-- ⭐ Spring AI Ollama（支持Function Calling） -->
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
- `spring-ai-starter-model-ollama`：本地Ollama模型，支持Function Calling

### 3.2 应用配置

```yaml
# 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/tools-ollama/src/main/resources/application.yml
server:
  port: 8081

spring:
  application:
    name: "tools-ollama"
  
  ai:
    ollama:
      base-url: "http://localhost:11434"
      chat:
        enabled: true
        model: qwen2.5  # 使用Qwen2.5模型（支持Function Calling）
```

**配置说明**：
- `model: qwen2.5`：Qwen2.5支持Function Calling功能
- 其他模型如llama3、mistral也支持

### 3.3 创建工具类

创建`DateTimeTools.java`，定义时间相关工具：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/tools-ollama/src/main/java/com/sandy/tools/ollama/DateTimeTools.java
package com.sandy.tools.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class DateTimeTools {

    /**
     * ⭐ 工具1：查询当前时间
     */
    @Tool(description = "查询用户所在时区的当前时间")
    String getCurrentDateTime() {
        log.info("getCurrentDateTime");
        
        // 获取当前时区的时间
        String currentDateTime = LocalDateTime.now()
            .atZone(LocaleContextHolder.getTimeZone().toZoneId())
            .toString();
        
        log.info("getCurrentDateTime: {}", currentDateTime);
        return currentDateTime;
    }

    /**
     * ⭐ 工具2：设置闹钟
     */
    @Tool(description = "将用户闹钟设置为给定的时间，时间格式为 ISO-8601")
    public String setAlarm(String time) {
        log.info("setAlarm with time: {}", time);
        
        // 解析时间
        LocalDateTime alarmTime = LocalDateTime.parse(
            time, 
            DateTimeFormatter.ISO_DATE_TIME
        );
        
        // 实际应用中，这里会调用系统API或存储到数据库
        String result = "Alarm set for " + alarmTime + ".";
        log.info(result);
        
        return result;
    }
}
```

**代码核心解析**：

1. **@Component注解**：
   ```java
   @Component
   public class DateTimeTools { ... }
   ```
   让Spring管理这个类，自动注入到需要的地方。

2. **@Tool注解**：
   ```java
   @Tool(description = "查询用户所在时区的当前时间")
   String getCurrentDateTime() { ... }
   ```
   - `description`：告诉AI这个工具的功能
   - AI会根据这个描述判断是否调用此函数

3. **方法签名**：
   ```java
   public String setAlarm(String time)
   ```
   - 参数名`time`会作为AI提取参数的依据
   - 返回值会作为AI生成回复的上下文

### 3.4 创建控制器

创建`AiController.java`，提供Function Calling API：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/tools-ollama/src/main/java/com/sandy/tools/ollama/AiController.java
package com.sandy.tools.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class AiController {
    
    @Autowired
    private ChatModel chatModel;
    
    @Autowired
    private DateTimeTools dateTimeTools;

    /**
     * ⭐ Function Calling 问答接口
     */
    @PostMapping("/ask")
    public String ask(@RequestBody String userQuery) {
        log.info("askQuery: {}", userQuery);
        
        // ⭐ 创建ChatClient并注册工具
        String response = ChatClient.create(chatModel)
            .prompt(userQuery)               // 用户问题
            .tools(new DateTimeTools())      // ⭐ 注册工具（关键！）
            .call()                          // 执行调用
            .content();                      // 获取回复内容
        
        log.info("askQuery: {}, response: {}", userQuery, response);
        return response;
    }
}
```

**代码核心解析**：

1. **注册工具**：
   ```java
   .tools(new DateTimeTools())
   ```
   将工具类注册给AI，AI就可以调用这些工具了。

2. **完整调用链**：
   ```
   用户问题 → ChatClient → AI分析 → 决定是否调用工具
                                ↓
                          调用工具函数
                                ↓
                          获取函数结果
                                ↓
                          生成自然语言回复
   ```

### 3.5 测试Function Calling

**启动应用**：

```bash
# 确保Ollama运行
ollama pull qwen2.5

# 启动应用
cd tools-ollama
mvn spring-boot:run
```

**测试时间查询**：

```bash
curl -X POST http://localhost:8081/ask \
  -H "Content-Type: text/plain" \
  -d "现在几点了？"

# AI内部流程：
# 1. 识别意图：查询时间
# 2. 调用：getCurrentDateTime()
# 3. 返回："现在是2025年12月3日下午2点30分。"
```

**测试闹钟设置**：

```bash
curl -X POST http://localhost:8081/ask \
  -H "Content-Type: text/plain" \
  -d "帮我设置明天早上8点的闹钟"

# AI内部流程：
# 1. 识别意图：设置闹钟
# 2. 提取参数：time="2025-12-04T08:00:00"
# 3. 调用：setAlarm("2025-12-04T08:00:00")
# 4. 返回："好的，已为您设置明天早上8点的闹钟。"
```

**日志输出**（观察调用过程）：

```
INFO  AiController - askQuery: 现在几点了？
INFO  DateTimeTools - getCurrentDateTime
INFO  DateTimeTools - getCurrentDateTime: 2025-12-03T14:30:00+08:00[Asia/Shanghai]
INFO  AiController - askQuery: 现在几点了？, response: 现在是2025年12月3日下午2点30分。
```

---

## 4. 参数提取与验证

### 4.1 AI如何提取参数？

AI会根据**参数名**和**函数描述**自动提取参数。

**示例**：

```java
@Tool(description = "将用户闹钟设置为给定的时间，时间格式为 ISO-8601")
public String setAlarm(String time) { ... }
```

用户输入："明天早上8点的闹钟"

AI处理：
1. 识别时间：明天早上8点
2. 转换格式：ISO-8601 → "2025-12-04T08:00:00"
3. 映射参数：time="2025-12-04T08:00:00"

### 4.2 多参数函数

```java
@Tool(description = "查询指定城市的天气，返回温度和天气状况")
public String getWeather(String city, String date) {
    log.info("getWeather: city={}, date={}", city, date);
    
    // 模拟天气查询
    return String.format("%s在%s的天气：晴天，20-28℃", city, date);
}
```

用户输入："查询北京明天的天气"

AI提取：
- `city` = "北京"
- `date` = "2025-12-04"

### 4.3 参数验证

在函数内部添加验证逻辑：

```java
@Tool(description = "设置提醒，时间不能早于当前时间")
public String setReminder(String time, String message) {
    // ⭐ 参数验证
    LocalDateTime reminderTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
    LocalDateTime now = LocalDateTime.now();
    
    if (reminderTime.isBefore(now)) {
        return "错误：提醒时间不能早于当前时间";
    }
    
    // 业务逻辑
    return String.format("已设置提醒：%s - %s", time, message);
}
```

### 4.4 复杂对象参数

使用自定义类作为参数：

```java
public class AlarmRequest {
    private String time;
    private String label;
    private boolean repeat;
    private String[] days;  // ["Monday", "Wednesday"]
    
    // getters and setters
}

@Tool(description = "设置复杂的重复闹钟")
public String setComplexAlarm(AlarmRequest request) {
    log.info("setComplexAlarm: {}", request);
    
    if (request.isRepeat()) {
        return String.format("已设置重复闹钟：%s，在%s重复", 
            request.getTime(), 
            String.join(", ", request.getDays()));
    }
    
    return "已设置单次闹钟：" + request.getTime();
}
```

AI会自动将JSON格式的参数映射到对象。

---

## 5. 多函数调用编排

### 5.1 注册多个工具

```java
@PostMapping("/ask-multi-tool")
public String askWithMultipleTools(@RequestBody String userQuery) {
    // ⭐ 注册多个工具类
    String response = ChatClient.create(chatModel)
        .prompt(userQuery)
        .tools(
            new DateTimeTools(),       // 时间工具
            new WeatherTools(),        // 天气工具
            new CalculatorTools(),     // 计算器工具
            new DatabaseTools()        // 数据库工具
        )
        .call()
        .content();
    
    return response;
}
```

### 5.2 AI如何选择工具？

AI会根据用户问题和工具描述自动选择：

```
用户："北京明天的天气怎么样？我需要设置早上8点的闹钟。"

AI分析：
1. 识别两个意图
2. 选择工具：
   - getWeather("北京", "2025-12-04")  ← WeatherTools
   - setAlarm("2025-12-04T08:00:00")  ← DateTimeTools
3. 依次调用
4. 综合结果生成回复
```

### 5.3 工具调用链

```java
// 扩展工具类：计算器
@Component
public class CalculatorTools {
    
    @Tool(description = "执行基本的数学计算，支持加减乘除")
    public double calculate(String expression) {
        // 简化示例，实际应使用表达式解析器
        log.info("calculate: {}", expression);
        
        // 模拟计算
        return 42.0;
    }
}

// 扩展工具类：数据库查询
@Component
public class DatabaseTools {
    
    @Tool(description = "查询数据库中的用户信息")
    public String queryUser(String userId) {
        log.info("queryUser: {}", userId);
        
        // 模拟数据库查询
        return "User{id=" + userId + ", name='Alice', email='alice@example.com'}";
    }
}
```

**复杂场景**：

```
用户："查询用户123的信息，然后计算他的年龄是否大于30"

AI执行：
1. 调用 queryUser("123") → 获取用户信息
2. 提取年龄 → 28
3. 调用 calculate("28 > 30") → false
4. 生成回复："用户123是Alice，年龄28岁，未满30岁。"
```

---

## 6. 安全防护与最佳实践

### 6.1 敏感操作防护

**问题**：如何防止AI误删数据？

```java
@Tool(description = "删除用户账号（危险操作，需要确认）")
public String deleteUser(String userId, boolean confirmed) {
    // ⭐ 安全检查
    if (!confirmed) {
        return "警告：删除操作需要确认。请再次确认是否删除用户 " + userId;
    }
    
    // 额外的权限检查
    if (!hasDeletePermission()) {
        return "错误：无权限执行删除操作";
    }
    
    // 执行删除
    log.warn("删除用户: {}", userId);
    return "用户 " + userId + " 已删除";
}
```

### 6.2 白名单机制

只暴露安全的工具：

```java
@Configuration
public class ToolsConfig {
    
    @Bean
    public List<Object> allowedTools() {
        return List.of(
            new DateTimeTools(),      // ✅ 只读，安全
            new WeatherTools(),       // ✅ 只读，安全
            new CalculatorTools()     // ✅ 无副作用，安全
            // ❌ 不注册 DatabaseWriteTools（写操作）
        );
    }
}
```

### 6.3 日志与审计

记录所有工具调用：

```java
@Aspect
@Component
public class ToolCallAudit {
    
    @Around("@annotation(org.springframework.ai.tool.annotation.Tool)")
    public Object auditToolCall(ProceedingJoinPoint joinPoint) throws Throwable {
        String toolName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.info("工具调用: {} with args: {}", toolName, args);
        
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long duration = System.currentTimeMillis() - start;
        
        log.info("工具调用完成: {} in {}ms, result: {}", toolName, duration, result);
        
        // 写入审计日志
        auditLog(toolName, args, result, duration);
        
        return result;
    }
}
```

### 6.4 错误处理

```java
@Tool(description = "查询外部API获取数据")
public String queryExternalApi(String endpoint) {
    try {
        // 调用外部API
        return restTemplate.getForObject(endpoint, String.class);
    } catch (RestClientException e) {
        log.error("API调用失败: {}", e.getMessage());
        // ⭐ 返回友好的错误信息给AI
        return "抱歉，外部服务暂时不可用，请稍后再试。";
    } catch (Exception e) {
        log.error("未知错误", e);
        return "发生了未知错误，请联系管理员。";
    }
}
```

### 6.5 超时控制

```java
@Tool(description = "执行耗时操作")
@Timeout(value = 5, unit = TimeUnit.SECONDS)  // 5秒超时
public String longRunningTask(String taskId) {
    // 长时间运行的任务
    return performTask(taskId);
}
```

### 6.6 最佳实践清单

**设计工具时**：
- ✅ 工具描述清晰准确
- ✅ 参数名具有语义
- ✅ 返回值是自然语言或结构化数据
- ✅ 添加必要的验证和错误处理
- ✅ 记录详细的日志

**安全考虑**：
- ✅ 敏感操作需要二次确认
- ✅ 实现权限检查
- ✅ 只暴露安全的工具
- ✅ 审计所有工具调用
- ✅ 设置超时和限流

**性能优化**：
- ✅ 缓存频繁调用的结果
- ✅ 异步执行耗时操作
- ✅ 限制单次可调用的工具数量

---

## 💻 示例代码

完整项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/tools-ollama](https://github.com/Mark7766/spring-ai-apps/tree/main/tools-ollama)

**项目结构**：
```
tools-ollama/
├── src/main/java/com/sandy/tools/ollama/
│   ├── ToolsOllamaApplication.java    # 启动类
│   ├── DateTimeTools.java              # 时间工具
│   └── AiController.java               # Function Calling API
├── src/main/resources/
│   ├── application.yml                 # 配置文件
│   └── static/chat.html                # 前端界面
└── pom.xml
```

**核心文件**：
- **DateTimeTools.java**：2个@Tool工具实现
- **AiController.java**：Function Calling调用示例

---

## 🤔 思考题

1. **如何设计Function的描述，让AI更准确地选择合适的工具？**
   
   提示：使用清晰的动词、包含关键词、说明参数格式、注明限制条件。

2. **多个函数调用如何保证原子性和一致性？**
   
   提示：考虑事务管理、补偿机制、幂等性设计。

3. **如何防止AI滥用某些敏感函数（如删除操作）？**
   
   提示：二次确认、权限检查、白名单机制、审计日志。

---

## 📖 拓展阅读

- [Spring AI Function Calling文档](https://docs.spring.io/spring-ai/reference/api/functions.html)
- [OpenAI Function Calling指南](https://platform.openai.com/docs/guides/function-calling)
- [Ollama Function Calling支持](https://ollama.ai/blog/function-calling)
- [LangChain Tools概念](https://python.langchain.com/docs/modules/tools/)

---

## ⏭️ 下期预告

恭喜你掌握了Function Calling技术！🎉 现在你的AI已经可以主动调用工具解决问题了。

**下一期我们将学习Text-to-SQL**，让业务人员用自然语言直接查询数据库，无需编写SQL！

**下期亮点**：
- 🗄️ 自然语言转SQL技术原理
- 📊 H2数据库集成与测试
- 🔍 复杂查询场景处理
- 🛡️ SQL注入防护策略
- 📈 查询结果可视化

敬请期待！

---

**更新日期**：2025年12月3日  
**状态**：✅ 已完成



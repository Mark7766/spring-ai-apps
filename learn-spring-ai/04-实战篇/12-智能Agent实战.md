# 第12期：智能Agent实战 - 打造自动化内容编辑助手

## 📌 本期概述

**核心问题：如何构建一个能自主运行的智能Agent？**

智能Agent是AI应用的高级形态，它能够自主感知环境、制定计划并执行任务。本期将实战一个完整的新闻采集与内容总结Agent——Newton，它每6小时自动抓取Hacker News热门技术文章，使用AI进行筛选、翻译、总结，并通过邮件推送，实现完全自动化的内容运营流程。

## 🎯 学习目标

完成本期学习后，你将能够：
- ✅ 理解智能Agent的核心架构和工作原理
- ✅ 实现多数据源的内容采集策略
- ✅ 使用AI进行内容筛选和总结
- ✅ 构建自动化任务调度系统
- ✅ 集成邮件推送功能
- ✅ 实现Agent的监控与日志管理

## 📚 内容大纲

### 1. 智能Agent架构设计

### 2. 新闻采集引擎实现

### 3. AI内容筛选与总结

### 4. 定时任务调度

### 5. 邮件推送集成

### 6. 监控与日志管理

---

## 1. 智能Agent架构设计

### 1.1 什么是智能Agent？

**智能Agent** 是一个能够自主感知、决策和执行的程序实体。

```
传统应用：
用户请求 → 应用处理 → 返回结果

智能Agent：
环境感知 → 自主决策 → 自动执行 → 持续监控
   ↓          ↓          ↓          ↓
定时触发   AI筛选     多步操作    状态记录
```

### 1.2 Newton Agent架构

```
┌─────────────────────────────────────┐
│         Newton Agent                │
│                                     │
│  ┌────────────────────────────┐    │
│  │  Scheduler (定时调度器)     │    │
│  │  - 每6小时触发一次          │    │
│  └────────┬───────────────────┘    │
│           ↓                         │
│  ┌────────────────────────────┐    │
│  │  HackerNewsProcessor        │    │
│  │  - 获取热门新闻列表         │    │
│  │  - 过滤已读新闻             │    │
│  └────────┬───────────────────┘    │
│           ↓                         │
│  ┌────────────────────────────┐    │
│  │  StoryProcessor (AI处理)   │    │
│  │  - 内容筛选（AI识别）       │    │
│  │  - 内容翻译                 │    │
│  │  - 内容总结（AI生成）       │    │
│  └────────┬───────────────────┘    │
│           ↓                         │
│  ┌────────────────────────────┐    │
│  │  EmailSender (推送)        │    │
│  │  - 格式化邮件               │    │
│  │  - 发送到订阅者             │    │
│  └────────────────────────────┘    │
│                                     │
│  ┌────────────────────────────┐    │
│  │  DatabaseHelper (状态)     │    │
│  │  - SQLite存储已读状态       │    │
│  └────────────────────────────┘    │
└─────────────────────────────────────┘
```

### 1.3 Agent工作流程

```
【每6小时触发】
    ↓
1. 获取HN Top 50文章
    ↓
2. 过滤已读文章（SQLite去重）
    ↓
3. 过滤3天内的文章
    ↓
4. AI筛选：只保留AI/软件开发相关
    ↓
5. 翻译标题：英文→中文
    ↓
6. AI总结：生成100字摘要
    ↓
7. 格式化邮件内容
    ↓
8. 发送邮件推送
    ↓
9. 记录到本地文件
    ↓
【完成，等待下次触发】
```

---

## 2. 新闻采集引擎实现

现在开始构建Newton Agent！

项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/newston](https://github.com/Mark7766/spring-ai-apps/tree/main/newston)

### 2.1 项目依赖配置

```xml
<!-- 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/newston/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.0</version>
    </parent>
    
    <groupId>com.sandy</groupId>
    <artifactId>newston</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>Newton</name>
    <description>
        Your 24/7 tech sentinel curating Hacker News breakthroughs in code and AI, 
        delivering distilled intelligence every 6 hours with human-readable clarity.
    </description>
    
    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0-M6</spring-ai.version>
        <spring-ai-alibaba.version>1.0.0-M5.1</spring-ai-alibaba.version>
    </properties>
    
    <dependencies>
        <!-- ⭐ Spring AI OpenAI -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
            <version>1.0.0-M5</version>
        </dependency>
        
        <!-- ⭐ Spring AI Ollama（本地模型） -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
            <version>1.0.0-M5</version>
        </dependency>
        
        <!-- ⭐ Spring AI 阿里云（通义千问） -->
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-starter</artifactId>
            <version>${spring-ai-alibaba.version}</version>
        </dependency>
        
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- ⭐ SQLite数据库（存储已读状态） -->
        <dependency>
            <groupId>org.xerial</groupId>
            <artifactId>sqlite-jdbc</artifactId>
            <version>3.36.0.3</version>
        </dependency>
        
        <!-- ⭐ Jsoup（HTML解析） -->
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.16.1</version>
        </dependency>
        
        <!-- Gson（JSON处理） -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.12.0</version>
        </dependency>
        
        <!-- ⭐ JavaMail（邮件发送） -->
        <dependency>
            <groupId>com.sun.mail</groupId>
            <artifactId>javax.mail</artifactId>
            <version>1.6.2</version>
        </dependency>
    </dependencies>
</project>
```

**核心依赖说明**：
1. **多AI模型支持**：OpenAI、Ollama、阿里云，可灵活切换
2. **SQLite**：轻量级数据库，存储已读文章ID
3. **Jsoup**：解析HTML内容
4. **JavaMail**：发送邮件推送

### 2.2 核心处理器（第1/2部分）

创建`HackerNewsProcessor.java`：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/newston/src/main/java/com/sandy/newston/HackerNewsProcessor.java（第1部分）
package com.sandy.newston;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HackerNewsProcessor {
    
    @Autowired
    private StoryProcessor storyProcessor;
    
    @Autowired
    private EmailSender emailSender;

    /**
     * ⭐ 定时任务：每6小时执行一次
     * cron表达式从配置文件读取
     */
    @Scheduled(cron = "${sandy.newston.schedule}")
    public void process() {
        log.info("HackerNewsProcessor starts to process news.");
        
        try {
            // ⭐ Step 1: 初始化数据库
            DatabaseHelper.initDB();
            Class.forName("org.sqlite.JDBC");
            log.info("Database is inited.");
            
            // ⭐ Step 2: 获取HN Top 50文章
            List<String> topStories = storyProcessor.getTopStories();
            log.info("topStories: {}", topStories.toString());
            
            // ⭐ Step 3: 过滤新文章（去重+时间过滤）
            List<JsonObject> newStories = getNewStories(topStories);
            log.info("newStories: {}", newStories.toString());
            
            if (newStories.isEmpty()) {
                log.info("No new story found.");
                return;
            }
            
            // ⭐ Step 4: AI筛选感兴趣的文章
            List<JsonObject> favouriteStories = storyProcessor.filterSoftwareTitles(newStories);
            
            // ⭐ Step 5: 生成摘要
            List<String> summaries = summary(favouriteStories);
            
            // ⭐ Step 6: 记录到本地文件
            recordDateSummaries(summaries);
            
            // ⭐ Step 7: 发送邮件
            if (!summaries.isEmpty()) {
                emailSender.sendEmail(
                    "Hacker News " + DateTimeUtils.getCurrentTime(), 
                    String.join("\n", summaries)
                );
            }

        } catch (Exception e) {
            log.error("Main process failed: " + e.getMessage(), e);
        }
        
        log.info("HackerNewsProcessor has processed news completely.");
    }
```

继续处理器的辅助方法：

```java
    // 继续：https://github.com/Mark7766/spring-ai-apps/blob/main/newston/src/main/java/com/sandy/newston/HackerNewsProcessor.java（第2部分）
    
    /**
     * ⭐ 过滤新文章（去重+时间过滤）
     */
    private List<JsonObject> getNewStories(List<String> topStories) 
        throws SQLException, IOException {
        
        List<JsonObject> newStories = new ArrayList<>();
        ZonedDateTime threeDaysAgo = ZonedDateTime.now(ZoneOffset.UTC).minusDays(3);
        
        // 只处理前50篇
        for (String storyId : topStories.subList(0, Math.min(50, topStories.size()))) {
            // ⭐ 检查是否已读（SQLite去重）
            if (!DatabaseHelper.keyExists(storyId)) {
                DatabaseHelper.setKey(storyId);
                
                // 获取文章详情
                JsonObject detail = storyProcessor.getStoryDetails(storyId);
                long time = detail.get("time").getAsLong();
                Instant instant = Instant.ofEpochSecond(time);
                
                // ⭐ 只保留3天内的文章
                if (instant.isAfter(threeDaysAgo.toInstant())) {
                    newStories.add(detail);
                }
            }
        }
        
        return newStories;
    }

    /**
     * ⭐ 生成摘要（翻译+总结）
     */
    private List<String> summary(List<JsonObject> favouriteStories) {
        List<String> summaries = new ArrayList<>();
        
        favouriteStories.forEach(detail -> {
            log.info("Processing detail: {}", detail.toString());
            
            if (detail.has("text")) {
                String title = detail.get("title").getAsString();
                String createDate = toDateStr(detail.get("time").getAsLong());
                int score = detail.get("score").getAsInt();
                int descendants = detail.get("descendants").getAsInt();
                String storyType = detail.get("type").getAsString();
                String text = detail.get("text").getAsString();
                String url = !detail.has("url") ? "NO URL" : detail.get("url").getAsString();
                
                try {
                    // ⭐ AI翻译标题
                    String titleZh = storyProcessor.translateToChinese(title);
                    
                    // ⭐ AI生成摘要
                    String summary = storyProcessor.generateSummary(text);
                    
                    // 格式化输出
                    summaries.add(String.format(
                        "%s:%d:%d:%s\n%s\n%s\n%s\n%s\n%s\n",
                        createDate, score, descendants, storyType,
                        title, titleZh, summary, url,
                        "-----------------------------"
                    ));
                } catch (IOException e) {
                    log.error("Translation error: {}", e.getMessage(), e);
                }
            }
        });
        
        return summaries;
    }

    /**
     * 记录到本地文件
     */
    private void recordDateSummaries(List<String> summaries) throws IOException {
        String filename = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".txt";
        try (FileWriter writer = new FileWriter(filename, true)) {
            for (String summary : summaries) {
                writer.write(summary + "\n");
            }
        }
    }

    /**
     * Unix时间戳转日期字符串
     */
    public static String toDateStr(long unixTimestamp) {
        Instant instant = Instant.ofEpochSecond(unixTimestamp);
        LocalDateTime localTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localTime.format(formatter);
    }
}
```

**核心逻辑解析**：

1. **@Scheduled注解**：
   ```java
   @Scheduled(cron = "${sandy.newston.schedule}")
   ```
   定时触发，cron表达式从配置读取。

2. **SQLite去重**：
   ```java
   if (!DatabaseHelper.keyExists(storyId)) {
       DatabaseHelper.setKey(storyId);
   ```
   避免重复处理同一文章。

3. **时间过滤**：
   ```java
   ZonedDateTime threeDaysAgo = ...
   if (instant.isAfter(threeDaysAgo.toInstant())) {
   ```
   只处理3天内的新鲜内容。

---

## 3. AI内容筛选与总结

### 3.1 Story处理器（第1/3部分）

创建`StoryProcessor.java`：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/newston/src/main/java/com/sandy/newston/StoryProcessor.java（第1部分）
package com.sandy.newston;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.google.gson.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.prompt.*;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

@Component
@Slf4j
public class StoryProcessor {
    
    private static final Gson gson = new Gson();
    private static final String DEFAULT_PROMPT = "你是一个博学的智能聊天助手，请根据用户提问回答！";
    
    private ChatClient dashScopeChatClient;
    private final ChatClient.Builder chatClientBuilder;
    
    @Value("${sandy.newston.bl-mode}")
    private String model;
    
    @Value("${spring.ai.dashscope.chat.enabled}")
    private boolean dashscopeEnabled;
    
    @Value("${spring.ai.openai.chat.enabled}")
    private boolean openaiEnabled;
    
    @Value("${spring.ai.ollama.chat.enabled}")
    private boolean ollamaEnabled;

    public StoryProcessor(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    /**
     * ⭐ 初始化AI模型（支持多种模型）
     */
    @PostConstruct
    public void init() {
        ChatOptions chatOptions = null;
        
        // ⭐ 根据配置选择AI模型
        if (dashscopeEnabled) {
            chatOptions = DashScopeChatOptions.builder()
                .withModel(this.model)
                .withTopP(0.7)
                .build();
        } else if (openaiEnabled) {
            chatOptions = OpenAiChatOptions.builder()
                .model("deepseek-chat")
                .temperature(0.4)
                .build();
        } else if (ollamaEnabled) {
            chatOptions = OllamaOptions.builder()
                .model(OllamaModel.QWEN_2_5_7B)
                .temperature(0.4)
                .build();
        } else {
            throw new RuntimeException("No AI model configured");
        }

        log.info("chatOptions: {}", gson.toJson(chatOptions));
        
        // ⭐ 构建ChatClient
        this.dashScopeChatClient = this.chatClientBuilder
            .defaultSystem(DEFAULT_PROMPT)
            .defaultAdvisors(
                new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                new SimpleLoggerAdvisor()
            )
            .defaultOptions(chatOptions)
            .build();
    }
```

继续Story处理器的API调用方法：

```java
    // 继续：https://github.com/Mark7766/spring-ai-apps/blob/main/newston/src/main/java/com/sandy/newston/StoryProcessor.java（第2部分）
    
    /**
     * ⭐ 获取HN热门文章列表
     */
    public List<String> getTopStories() throws IOException {
        JsonArray topStories = HttpUtil.get(
            "https://hacker-news.firebaseio.com/v0/topstories.json", 
            JsonArray.class
        );
        
        List<String> result = new ArrayList<>();
        for (JsonElement el : topStories) {
            result.add(el.getAsString());
        }
        return result;
    }

    /**
     * ⭐ 获取文章详情
     */
    public JsonObject getStoryDetails(String storyId) throws IOException {
        return HttpUtil.get(
            "https://hacker-news.firebaseio.com/v0/item/" + storyId + ".json", 
            JsonObject.class
        );
    }

    /**
     * ⭐ 翻译标题（使用Google翻译API）
     */
    public String translateToChinese(String text) throws IOException {
        JsonArray response = HttpUtil.get(
            "https://translate.googleapis.com/translate_a/single?client=gtx&sl=en&tl=zh-CN&dt=t&q=" 
            + URLEncoder.encode(text, "UTF-8"), 
            JsonArray.class
        );
        return response.get(0).getAsJsonArray()
            .get(0).getAsJsonArray()
            .get(0).getAsString();
    }
```

继续AI内容处理方法：

```java
    // 继续：https://github.com/Mark7766/spring-ai-apps/blob/main/newston/src/main/java/com/sandy/newston/StoryProcessor.java（第3部分）
    
    /**
     * ⭐ AI生成摘要
     */
    public String generateSummary(String text) {
        String promptStr = "基于" + text + "生成100字以内的中文摘要，不需要英文摘要和中文描述";
        
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你一个AI和IT技术专家，也是IT技术博主的助手。"));
        messages.add(new UserMessage(promptStr));
        
        Prompt prompt = new Prompt(messages);
        log.info("generateSummary request: {}", gson.toJson(prompt));
        
        String content = dashScopeChatClient.prompt(prompt).call().content();
        log.info("generateSummary response: {}", content);
        
        return content;
    }

    /**
     * ⭐ AI筛选技术相关文章
     */
    public List<JsonObject> filterSoftwareTitles(List<JsonObject> newStories) {
        // 构建ID和Story的映射
        Map<String, JsonObject> idStoryMapping = new HashMap<>();
        StringBuilder titles = new StringBuilder();
        
        newStories.forEach(story -> {
            String id = story.get("id").getAsString();
            String title = story.get("title").getAsString();
            titles.append(title).append(" ").append(id).append("\n");
            idStoryMapping.put(id, story);
        });
        
        // ⭐ 构建AI筛选Prompt
        String promptStr = """
            以下是一些故事的标题。请过滤出与AI或软件开发相关的标题和对应的ID,
            ID不要用括号括起来,ID后面不用有空格，ID在标题的后边，如标题 ID，
            回复的内容里只保留标题和ID，如果没有合适的标题，请直接回复没有，
            故事的标题:
            """ + titles.toString();
        
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage("你一个AI和IT技术专家，也是IT技术博主的助手。"));
        messages.add(new UserMessage(promptStr));
        
        Prompt prompt = new Prompt(messages);
        log.info("filterSoftwareTitles request: {}", gson.toJson(prompt));
        
        String content = dashScopeChatClient.prompt(prompt).call().content();
        log.info("filterSoftwareTitles response: {}", content);
        
        // ⭐ 解析AI返回的结果
        String[] lines = content.split("\n");
        List<JsonObject> result = new ArrayList<>();
        
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                String[] parts = line.split(" ");
                if (parts.length > 0) {
                    String id = parts[parts.length - 1];
                    if (idStoryMapping.containsKey(id)) {
                        result.add(idStoryMapping.get(id));
                    } else {
                        log.info("Title[{}] has no story.", id);
                    }
                }
            }
        }
        
        return result;
    }
}
```

**AI处理核心逻辑**：

1. **AI筛选**：
   ```java
   filterSoftwareTitles(newStories)
   ```
   将所有标题发送给AI，让AI判断哪些是技术相关。

2. **AI摘要**：
   ```java
   generateSummary(text)
   ```
   AI生成100字中文摘要，提炼核心内容。

3. **多模型支持**：
   根据配置文件选择使用阿里云、OpenAI还是Ollama。

---

## 4. 定时任务调度

### 4.1 配置定时任务

在`application.yml`中配置：

```yaml
# 定时任务cron表达式
sandy:
  newston:
    schedule: "0 0 0/6 * * ?"  # 每6小时执行一次
    bl-mode: "qwen-plus"  # AI模型
```

**cron表达式解析**：
- `0 0 0/6 * * ?`
  - 秒：0
  - 分：0
  - 时：0/6（每6小时）
  - 日：*（每天）
  - 月：*（每月）
  - 周：?（不指定）

### 4.2 启用定时任务

在启动类添加注解：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/newston/src/main/java/com/sandy/newston/NewstonApplication.java
package com.sandy.newston;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // ⭐ 启用定时任务
public class NewstonApplication {
    public static void main(String[] args) {
        SpringApplication.run(NewstonApplication.java, args);
    }
}
```

---

## 5. 邮件推送集成

### 5.1 邮件发送器

创建`EmailSender.java`：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/newston/src/main/java/com/sandy/newston/EmailSender.java
package com.sandy.newston;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

@Slf4j
@Component
public class EmailSender {
    
    @Value("${email.smtp.host}")
    private String smtpHost;
    
    @Value("${email.smtp.port}")
    private int smtpPort;
    
    @Value("${email.from}")
    private String from;
    
    @Value("${email.password}")
    private String password;
    
    @Value("${email.to}")
    private String to;

    /**
     * ⭐ 发送邮件
     */
    public void sendEmail(String subject, String body) {
        try {
            // ⭐ 配置邮件服务器
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            
            // ⭐ 创建会话
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(from, password);
                }
            });
            
            // ⭐ 构建邮件
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            
            // ⭐ 发送
            Transport.send(message);
            log.info("Email sent successfully to {}", to);
            
        } catch (MessagingException e) {
            log.error("Failed to send email: {}", e.getMessage(), e);
        }
    }
}
```

### 5.2 邮件配置

在`application.yml`中配置：

```yaml
email:
  smtp:
    host: smtp.gmail.com  # SMTP服务器
    port: 587             # 端口
  from: your-email@gmail.com
  password: your-app-password  # 应用专用密码
  to: subscriber@example.com
```

---

## 6. 监控与日志管理

### 6.1 SQLite数据库助手

创建`DatabaseHelper.java`：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/newston/src/main/java/com/sandy/newston/DatabaseHelper.java
package com.sandy.newston;

import java.sql.*;

public class DatabaseHelper {
    
    private static final String DB_URL = "jdbc:sqlite:newston.db";

    /**
     * ⭐ 初始化数据库
     */
    public static void initDB() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // 创建表（如果不存在）
            String sql = """
                CREATE TABLE IF NOT EXISTS seen_stories (
                    story_id TEXT PRIMARY KEY,
                    seen_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            stmt.execute(sql);
        }
    }

    /**
     * ⭐ 检查文章是否已读
     */
    public static boolean keyExists(String storyId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT 1 FROM seen_stories WHERE story_id = ?")) {
            
            pstmt.setString(1, storyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * ⭐ 标记文章为已读
     */
    public static void setKey(String storyId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT OR IGNORE INTO seen_stories (story_id) VALUES (?)")) {
            
            pstmt.setString(1, storyId);
            pstmt.executeUpdate();
        }
    }
}
```

### 6.2 日志配置

配置`logback.xml`：

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/newston.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/newston.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

---

## 💻 示例代码

完整项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/newston](https://github.com/Mark7766/spring-ai-apps/tree/main/newston)

**项目结构**：
```
newston/
├── src/main/java/com/sandy/newston/
│   ├── NewstonApplication.java          # 启动类
│   ├── HackerNewsProcessor.java         # 主流程处理器
│   ├── StoryProcessor.java              # AI内容处理
│   ├── EmailSender.java                 # 邮件发送
│   ├── DatabaseHelper.java              # SQLite助手
│   ├── HttpUtil.java                    # HTTP工具
│   └── DateTimeUtils.java               # 时间工具
├── src/main/resources/
│   ├── application.yml                  # 配置文件
│   └── logback.xml                      # 日志配置
├── Dockerfile                           # Docker镜像
└── pom.xml
```

---

## 🤔 思考题

1. **如何让Agent在任务失败时自动重试和恢复？**
   
   提示：可以使用Spring Retry、死信队列、状态机等方案。

2. **多个Agent如何协作完成复杂任务？**
   
   提示：考虑消息队列、事件驱动、微服务编排等架构。

3. **如何评估Agent的工作质量？**
   
   提示：可以记录成功率、响应时间、用户反馈等指标。

---

## 📖 拓展阅读

- [Spring Scheduling文档](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Hacker News API](https://github.com/HackerNews/API)
- [智能Agent设计模式](https://arxiv.org/abs/2308.11432)
- [LangChain Agent文档](https://python.langchain.com/docs/modules/agents/)

---

## ⏭️ 下期预告

恭喜你掌握了智能Agent实战！🎉 现在你可以构建自动化运行的AI应用了。

但单个Agent还不够，企业级应用需要考虑更多：
- ❓ 如何支撑百万级并发？
- ❓ 如何保证99.99%的可用性？
- ❓ 如何设计可扩展的RAG架构？

**下一期我们将学习企业级RAG系统架构设计**，从技术选型到高可用部署，构建生产级AI应用！

**下期亮点**：
- 🏗️ 企业级RAG架构设计模式
- 🚀 高性能向量检索优化
- 🔄 分布式系统设计
- 📊 监控与可观测性
- 🛡️ 安全与权限管理
- 💰 成本优化策略

从单机到集群，从原型到生产，让你的AI应用真正落地！

敬请期待！

---

**更新日期**：2025年12月3日  
**状态**：✅ 已完成



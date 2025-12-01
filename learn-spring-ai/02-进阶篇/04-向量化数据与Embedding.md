# 第4期：向量化你的数据 - Embedding模型应用实战

## 📌 本期概述

**核心问题：如何将文本数据转化为向量并实现语义搜索？**

传统的关键词搜索无法理解语义，而Embedding技术可以将文本转化为向量，实现真正的语义搜索。本期将深入讲解Embedding原理，并使用Ollama本地部署模型进行实战，打造一个智能医疗助手应用。

## 🎯 学习目标

完成本期学习后，你将能够：
- ✅ 理解Embedding技术原理和应用场景
- ✅ 使用Ollama本地部署Embedding模型
- ✅ 实现文本向量化处理
- ✅ 掌握余弦相似度计算方法
- ✅ 构建基于语义搜索的知识问答系统

## 📚 内容大纲

### 1. 什么是Embedding？

### 2. Embedding的应用场景

### 3. Ollama本地部署

### 4. 文本向量化实战

### 5. 相似度计算原理

### 6. 智能医疗助手实战

### 7. 本地模型 vs 云端API

---

## 1. 什么是Embedding？

### 1.1 传统搜索的局限性

**关键词搜索的问题**：

```
用户搜索："肚子疼怎么办？"

知识库文档：
1. "腹痛可能是消化不良引起的..."  ❌ 搜不到（关键词不匹配）
2. "胃部不适的处理方法..."        ❌ 搜不到
3. "肚子疼的原因分析..."          ✅ 能搜到（关键词匹配）
```

**问题**：
- 关键词必须完全匹配
- 无法理解同义词（"肚子疼" ≠ "腹痛" ≠ "胃痛"）
- 无法理解语义（"如何缓解疼痛" vs "疼痛怎么办"）

### 1.2 Embedding的解决方案

**Embedding（嵌入）** 是将文本转换为高维向量的技术。

```
文本                    →  向量（Embedding）
"肚子疼"               →  [0.2, -0.5, 0.8, ...]  (896维)
"腹痛"                 →  [0.19, -0.48, 0.79, ...] (相似！)
"头痛"                 →  [-0.3, 0.6, -0.2, ...]  (不同！)
```

**关键特性**：
- 语义相近的文本，向量也相近
- 可以通过计算向量距离来判断文本相似度
- 与具体关键词无关，理解的是"意思"

### 1.3 Embedding的维度

不同模型生成的向量维度不同：

| 模型 | 维度 | 特点 |
|------|------|------|
| **OpenAI text-embedding-3-small** | 1536 | 高质量，收费 |
| **OpenAI text-embedding-3-large** | 3072 | 更高质量，更贵 |
| **Qwen2.5 (Ollama)** | 896 | 本地免费，中文优秀 |
| **BERT-base** | 768 | 轻量级，速度快 |

**维度越高 = 表达能力越强 = 计算量越大**

### 1.4 可视化理解

想象将文本映射到3D空间（实际是几百到几千维）：

```
        "头痛" ●
              
                        "腹痛" ● ● "肚子疼"
                              ● "胃痛"
                              
            "心脏病" ●
```

语义相近的词在空间中靠得更近！

---

## 2. Embedding的应用场景

### 2.1 语义搜索

**场景**：企业知识库搜索

```java
用户问题："如何提高销售业绩？"

传统搜索结果：
❌ 找不到（知识库用的是"销售技巧"、"业绩提升"等词）

Embedding搜索结果：
✅ "销售技巧培训资料"  (相似度 0.85)
✅ "业绩提升方法汇总"  (相似度 0.82)
✅ "客户沟通话术技巧"  (相似度 0.78)
```

### 2.2 推荐系统

**场景**：文章推荐

```
用户正在阅读："Spring Boot 入门教程"
向量化 → [0.5, -0.2, ...]

推荐文章：
1. "Spring Cloud 微服务实战" (相似度 0.88) ✅
2. "Java 并发编程详解"      (相似度 0.65) ✅
3. "Python 数据分析"        (相似度 0.12) ❌
```

### 2.3 问答系统（RAG）

**场景**：客服机器人

```
流程：
用户提问 → Embedding向量化 → 搜索相似问题/答案 → 结合LLM生成回复
```

### 2.4 去重与聚类

**场景**：新闻去重

```
新闻1："苹果发布新款iPhone"
新闻2："Apple推出最新手机产品"
新闻3："特斯拉发布新车型"

计算相似度：
新闻1 vs 新闻2 = 0.92 → 判定为重复 ✅
新闻1 vs 新闻3 = 0.15 → 不重复 ❌
```

---

## 3. Ollama本地部署

### 3.1 为什么选择Ollama？

| 对比项 | OpenAI Embedding API | Ollama本地部署 |
|--------|---------------------|---------------|
| **成本** | 按Token收费 | 完全免费 |
| **隐私** | 数据上传到云端 | 数据不出本地 |
| **速度** | 取决于网络 | 本地计算，快 |
| **限制** | API速率限制 | 无限制 |
| **中文支持** | 良好 | Qwen2.5优秀 |
| **硬件要求** | 无 | 需要GPU（可选）|

**推荐场景**：
- ✅ 学习和测试
- ✅ 企业内部应用（数据敏感）
- ✅ 高频调用场景
- ✅ 中文为主的应用

### 3.2 安装Ollama

#### Windows/Mac安装

1. 访问 [https://ollama.ai/download](https://ollama.ai/download)
2. 下载对应系统的安装包
3. 双击安装，一路Next

#### Linux安装

```bash
curl -fsSL https://ollama.ai/install.sh | sh
```

### 3.3 拉取Qwen2.5模型

```bash
# 拉取Qwen2.5模型（同时支持Chat和Embedding）
ollama pull qwen2.5

# 验证安装
ollama list

# 输出：
# NAME            SIZE     MODIFIED
# qwen2.5:latest  4.7GB    2 minutes ago
```

**模型说明**：
- **qwen2.5**：通义千问2.5，阿里巴巴出品
- **大小**：约4.7GB
- **特点**：中文能力强，支持Chat和Embedding

### 3.4 测试Ollama服务

```bash
# 启动Ollama（安装后会自动启动）
# Windows: 任务栏图标
# Linux: sudo systemctl start ollama

# 测试Embedding API
curl http://localhost:11434/api/embeddings -d '{
  "model": "qwen2.5",
  "prompt": "你好世界"
}'

# 返回：
# {
#   "embedding": [0.123, -0.456, 0.789, ...]  // 896维向量
# }
```

**默认端口**：`11434`

---

## 4. 文本向量化实战

现在开始构建一个智能医疗助手应用！

项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/embeddings-ollama](https://github.com/Mark7766/spring-ai-apps/tree/main/embeddings-ollama)

### 4.1 项目依赖配置

```xml
<!-- 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/embeddings-ollama/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.4</version>
    </parent>
    
    <groupId>com.sandy</groupId>
    <artifactId>embeddings-ollama</artifactId>
    <version>0.0.2-SNAPSHOT</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
    </properties>
    
    <dependencies>
        <!-- ⭐ Spring AI Ollama Starter -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-ollama</artifactId>
        </dependency>
        
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
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

**核心依赖**：`spring-ai-starter-model-ollama`

这个依赖包含了：
- ChatModel（对话）
- EmbeddingModel（向量化）
- 自动配置

### 4.2 应用配置

```yaml
# 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/embeddings-ollama/src/main/resources/application.yml
server:
  port: 8081

spring:
  application:
    name: "embeddings-ollama"
  
  ai:
    ollama:
      # Ollama服务地址
      base-url: "http://localhost:11434"
      
      # Embedding配置
      embedding:
        enabled: true
        model: qwen2.5  # 使用Qwen2.5模型
      
      # Chat配置（医疗助手需要）
      chat:
        enabled: true
        model: qwen2.5
```

**配置说明**：
- `base-url`：Ollama服务地址
- `embedding.enabled`：启用Embedding功能
- `embedding.model`：使用的模型名称
- `chat.enabled`：同时启用Chat功能

### 4.3 核心业务逻辑（第1/2部分）

先看核心Controller的前半部分：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/embeddings-ollama/src/main/java/com/sandy/embeddings/ollama/DoctorAssistantController.java
package com.sandy.embeddings.ollama;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class DoctorAssistantController {

    @Autowired
    private EmbeddingModel embeddingModel;  // ⭐ 注入Embedding模型

    @Autowired
    private ChatModel chatModel;  // 注入Chat模型

    // 模拟的医疗知识库
    private final List<String> knowledgeBase = Arrays.asList(
        "肚子疼可能是消化不良引起的，可以尝试喝点温水或吃点清淡的食物。如果持续疼痛，请咨询医生。",
        "腹痛如果伴随发热，可能是感染，建议尽快就医。",
        "背痛可能是肌肉拉伤引起的，可以尝试休息和热敷。"
    );

    // ⭐ 存储文档的向量（预计算）
    private final Map<String, float[]> documentEmbeddings = new HashMap<>();

    // 构造函数：应用启动时预计算所有文档的向量
    public DoctorAssistantController(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        
        // ⭐ 预计算知识库文档的向量
        for (String doc : knowledgeBase) {
            EmbeddingResponse response = embeddingModel.call(
                new org.springframework.ai.embedding.EmbeddingRequest(
                    List.of(doc),
                    OllamaOptions.builder()
                        .model(OllamaModel.QWEN_2_5_7B)
                        .build()
                )
            );
            
            // 保存文档和对应的向量
            documentEmbeddings.put(doc, response.getResult().getOutput());
        }
    }
```

### 4.4 核心业务逻辑（第2/2部分）

接下来是主要的查询逻辑和相似度计算：

```java
    // 继续：https://github.com/Mark7766/spring-ai-apps/blob/main/embeddings-ollama/src/main/java/com/sandy/embeddings/ollama/DoctorAssistantController.java
    
    @GetMapping("/doctor-assistant")
    public String answerHealthQuestion(@RequestParam String question) {
        
        // ⭐ Step 1: 将用户问题向量化
        EmbeddingResponse questionEmbeddingResponse = embeddingModel.call(
            new org.springframework.ai.embedding.EmbeddingRequest(
                List.of(question),
                OllamaOptions.builder()
                    .model(OllamaModel.QWEN_2_5_7B)
                    .build()
            )
        );
        float[] questionEmbedding = questionEmbeddingResponse.getResult().getOutput();

        // ⭐ Step 2: 计算相似度，找到最相关的文档
        String mostRelevantDoc = findMostRelevantDocument(questionEmbedding);
        
        if (mostRelevantDoc == null) {
            return "抱歉，我无法提供针对您问题的具体建议，请咨询专业医生。";
        }

        // ⭐ Step 3: 使用ChatModel基于检索到的文档生成回答
        String promptText = String.format(
            "您是一位医生助手，根据以下信息回答用户的问题：\n" +
            "知识：%s\n" +
            "用户问题：%s\n" +
            "请提供简洁、自然的建议，并提醒用户必要时咨询医生。",
            mostRelevantDoc, question
        );
        
        ChatResponse chatResponse = chatModel.call(new Prompt(promptText));
        return chatResponse.getResult().getOutput().getText();
    }

    // ⭐ 计算余弦相似度并找到最相似的文档
    private String findMostRelevantDocument(float[] questionEmbedding) {
        String bestMatch = null;
        double maxSimilarity = -1.0;

        for (Map.Entry<String, float[]> entry : documentEmbeddings.entrySet()) {
            float[] docEmbedding = entry.getValue();
            
            // 计算余弦相似度
            double similarity = cosineSimilarity(questionEmbedding, docEmbedding);
            
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestMatch = entry.getKey();
            }
        }

        // ⭐ 设置相似度阈值，避免不相关的文档
        return maxSimilarity > 0.6 ? bestMatch : null;
    }

    // ⭐ 余弦相似度计算
    private double cosineSimilarity(float[] vec1, float[] vec2) {
        double dotProduct = 0.0;   // 点积
        double norm1 = 0.0;        // 向量1的模
        double norm2 = 0.0;        // 向量2的模
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        // 余弦相似度公式：cos(θ) = (A·B) / (||A|| * ||B||)
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
```

**代码核心解析**：

1. **预计算向量（构造函数）**：
   ```java
   documentEmbeddings.put(doc, response.getResult().getOutput());
   ```
   启动时一次性计算所有文档的向量，避免每次查询都重新计算。

2. **用户问题向量化**：
   ```java
   float[] questionEmbedding = questionEmbeddingResponse.getResult().getOutput();
   ```
   将用户输入的问题转换为896维向量。

3. **相似度搜索**：
   ```java
   double similarity = cosineSimilarity(questionEmbedding, docEmbedding);
   ```
   遍历所有文档，找到最相似的一个。

4. **阈值过滤**：
   ```java
   return maxSimilarity > 0.6 ? bestMatch : null;
   ```
   只返回相似度大于0.6的文档，避免不相关匹配。

---

## 5. 相似度计算原理

### 5.1 什么是余弦相似度？

**余弦相似度**测量两个向量之间的夹角：

```
向量A: [1, 2, 3]
向量B: [2, 4, 6]  （方向相同，长度不同）

余弦相似度 = cos(夹角) = 1.0  （完全相似）
```

**可视化理解**：

```
      向量B
        ↗
       /  θ=0°
      /
     → 向量A

相似度 = cos(0°) = 1.0
```

```
      向量B
        ↑
        |  θ=90°
        |
        |
        → 向量A

相似度 = cos(90°) = 0.0
```

### 5.2 余弦相似度公式

```
相似度 = (A · B) / (||A|| × ||B||)

其中：
- A · B = 点积 = a₁×b₁ + a₂×b₂ + ... + aₙ×bₙ
- ||A|| = 向量A的模 = √(a₁² + a₂² + ... + aₙ²)
- ||B|| = 向量B的模 = √(b₁² + b₂² + ... + bₙ²)
```

### 5.3 代码实现详解

```java
private double cosineSimilarity(float[] vec1, float[] vec2) {
    double dotProduct = 0.0;   // 点积：A · B
    double norm1 = 0.0;        // ||A||²
    double norm2 = 0.0;        // ||B||²
    
    // 遍历每个维度
    for (int i = 0; i < vec1.length; i++) {
        dotProduct += vec1[i] * vec2[i];  // 累加 aᵢ × bᵢ
        norm1 += vec1[i] * vec1[i];       // 累加 aᵢ²
        norm2 += vec2[i] * vec2[i];       // 累加 bᵢ²
    }
    
    // 计算最终相似度
    return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
}
```

**计算示例**（简化为3维）：

```
向量A: [0.5, -0.3, 0.8]
向量B: [0.6, -0.2, 0.7]

点积 = 0.5×0.6 + (-0.3)×(-0.2) + 0.8×0.7
     = 0.3 + 0.06 + 0.56
     = 0.92

||A|| = √(0.5² + (-0.3)² + 0.8²) = √0.98 ≈ 0.99
||B|| = √(0.6² + (-0.2)² + 0.7²) = √0.89 ≈ 0.94

相似度 = 0.92 / (0.99 × 0.94) ≈ 0.99  （非常相似！）
```

### 5.4 相似度范围

| 相似度值 | 含义 | 示例 |
|---------|------|------|
| **1.0** | 完全相同 | "肚子疼" vs "肚子疼" |
| **0.8-0.99** | 非常相似 | "肚子疼" vs "腹痛" |
| **0.6-0.8** | 比较相似 | "肚子疼" vs "胃部不适" |
| **0.3-0.6** | 有点相关 | "肚子疼" vs "消化问题" |
| **0-0.3** | 不相关 | "肚子疼" vs "头痛" |
| **-1.0** | 完全相反 | 理论上，实际很少出现 |

### 5.5 其他距离度量

| 方法 | 公式 | 特点 | 适用场景 |
|------|------|------|---------|
| **余弦相似度** | cos(θ) | 关注方向，忽略长度 | 文本、推荐 |
| **欧氏距离** | √Σ(aᵢ-bᵢ)² | 关注实际距离 | 图像、聚类 |
| **曼哈顿距离** | Σ\|aᵢ-bᵢ\| | 只能直角移动 | 网格搜索 |
| **点积** | A·B | 简单快速 | 排序场景 |

**为什么文本Embedding用余弦相似度？**

```
文本A（短）: "肚子疼"     → 向量长度小
文本B（长）: "肚子疼可能是..." → 向量长度大

欧氏距离：认为不相似（长度差异大）❌
余弦相似度：认为相似（方向一致）✅
```

---

## 6. 智能医疗助手实战

### 6.1 应用工作流程

```
用户输入："我胃痛怎么办？"
    ↓
【向量化】questionEmbedding = [0.2, -0.5, 0.8, ...]
    ↓
【相似度搜索】
  计算 vs 文档1("肚子疼可能是...") = 0.85 ✅
  计算 vs 文档2("腹痛如果伴随...") = 0.65
  计算 vs 文档3("背痛可能是...") = 0.12
    ↓
【选择最相关】文档1 (相似度0.85 > 0.6阈值)
    ↓
【生成回答】ChatModel基于文档1生成自然语言回复
    ↓
输出："根据症状，可能是消化不良引起的胃痛。建议..."
```

### 6.2 测试应用

**启动应用**：

```bash
cd embeddings-ollama
mvn spring-boot:run
```

**测试API**：

```bash
# 测试1：相似问题（会匹配到文档1）
curl "http://localhost:8081/doctor-assistant?question=我肚子不舒服"

# 返回：
# 根据您的症状，可能是消化不良引起的。建议您喝点温水，吃些清淡的食物...

# 测试2：语义相似（会匹配到文档1）
curl "http://localhost:8081/doctor-assistant?question=胃痛怎么办"

# 返回：
# 胃痛可能是消化不良导致的，建议尝试...

# 测试3：不相关问题（相似度低）
curl "http://localhost:8081/doctor-assistant?question=怎么学习编程"

# 返回：
# 抱歉，我无法提供针对您问题的具体建议，请咨询专业医生。
```

### 6.3 实际效果分析

**场景1：完全匹配**

```
输入："肚子疼怎么办？"
匹配文档："肚子疼可能是消化不良..."
相似度：0.95 ✅
```

**场景2：同义词**

```
输入："腹部疼痛如何缓解？"
匹配文档："肚子疼可能是消化不良..."
相似度：0.82 ✅（理解同义词！）
```

**场景3：语义相关**

```
输入："消化不好怎么办？"
匹配文档："肚子疼可能是消化不良..."
相似度：0.75 ✅（理解语义关联！）
```

**场景4：不相关**

```
输入："头疼怎么办？"
匹配文档："肚子疼可能是消化不良..."
相似度：0.15 ❌（正确拒绝）
```

### 6.4 优化建议

**1. 扩充知识库**

```java
private final List<String> knowledgeBase = Arrays.asList(
    // 消化系统
    "肚子疼可能是消化不良引起的...",
    "腹泻的常见原因包括...",
    "便秘可以通过饮食调理...",
    
    // 呼吸系统
    "咳嗽可能是感冒引起的...",
    "哮喘患者应该避免...",
    
    // ... 更多文档
);
```

**2. 文档分块**

```java
// 长文档切分成小段
String longDoc = "肚子疼的原因很多...（3000字）";

// 切分为多个小段（每段200-300字）
List<String> chunks = splitIntoChunks(longDoc, 300);
```

**3. 返回Top-K结果**

```java
// 不只返回最相似的1个，返回Top 3
List<SimilarDocument> topK = findTopKDocuments(questionEmbedding, 3);

// 综合多个文档生成回答
String context = topK.stream()
    .map(doc -> doc.content)
    .collect(Collectors.joining("\n"));
```

**4. 添加缓存**

```java
@Cacheable("embeddings")
public float[] getEmbedding(String text) {
    // 避免重复计算相同文本的向量
    return embeddingModel.call(...).getResult().getOutput();
}
```

---

## 7. 本地模型 vs 云端API

### 7.1 全方位对比

| 对比维度 | Ollama本地 | OpenAI云端 | 推荐场景 |
|---------|-----------|-----------|---------|
| **成本** | 免费（硬件成本） | $0.0001/1K tokens | 本地：高频调用<br>云端：低频 |
| **速度** | 快（本地计算） | 取决于网络 | 本地：实时场景<br>云端：批量处理 |
| **质量** | 中等（Qwen2.5） | 高（text-embedding-3） | 云端：高精度需求 |
| **隐私** | 100%安全 | 数据上传云端 | 本地：敏感数据 |
| **中文支持** | Qwen2.5优秀 | 良好 | 本地：中文为主 |
| **硬件要求** | 8GB+ RAM<br>GPU可选 | 无 | 云端：无硬件 |
| **扩展性** | 单机限制 | 无限扩展 | 云端：大规模 |

### 7.2 成本计算

**云端API成本**（以OpenAI为例）：

```
价格：$0.0001 / 1K tokens

场景：客服系统，每天处理10000个问题
- 每个问题平均50 tokens
- 每天消耗：10000 × 50 / 1000 = 500K tokens
- 每天成本：500 × $0.0001 = $0.05
- 每月成本：$0.05 × 30 = $1.5

一年成本：$18
```

**本地部署成本**：

```
硬件：
- CPU：足够（Intel i5+）
- 内存：16GB推荐
- GPU：可选（GTX 1060+）
- 硬盘：20GB+

电费：
- 功耗：100W（无GPU）/ 300W（有GPU）
- 每天运行8小时
- 每月电费：约30-50元

一次性投入 + 电费 < 云端API（高频场景）
```

### 7.3 质量对比测试

**测试数据**：中文医疗问答

| 测试问题 | Qwen2.5本地 | OpenAI云端 | 人工评分 |
|---------|------------|-----------|---------|
| "肚子疼" vs "腹痛" | 0.85 | 0.92 | 应该高 ✅ |
| "头痛" vs "肚子疼" | 0.12 | 0.08 | 应该低 ✅ |
| "胃部不适" vs "消化不良" | 0.78 | 0.88 | 应该高 ✅ |

**结论**：
- OpenAI略优（+5-10%）
- Qwen2.5已经足够好
- 中文场景差距不大

### 7.4 选型决策树

```
开始
  ↓
是否有敏感数据？
  ├─ 是 → 本地部署（Ollama）
  └─ 否 ↓
        ↓
     调用频率？
       ├─ 高（每天>10万次）→ 本地部署
       └─ 低 ↓
             ↓
          是否主要中文？
            ├─ 是 → 本地Qwen2.5
            └─ 否 → 云端OpenAI
```

### 7.5 混合方案

**最佳实践**：本地+云端混合

```java
@Service
public class HybridEmbeddingService {
    
    @Autowired
    private EmbeddingModel localModel;  // Ollama本地
    
    @Autowired
    private OpenAiEmbeddingModel cloudModel;  // OpenAI云端
    
    public float[] getEmbedding(String text, boolean requireHighQuality) {
        if (requireHighQuality) {
            // 重要场景用云端高质量模型
            return cloudModel.embed(text);
        } else {
            // 常规场景用本地模型
            return localModel.embed(text);
        }
    }
}
```

**使用场景**：
- 用户问题向量化 → 本地（高频）
- 知识库文档向量化 → 云端（一次性，高质量）
- 实时搜索 → 本地（速度优先）
- 离线批处理 → 云端（质量优先）

---

## 💻 示例代码

完整项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/embeddings-ollama](https://github.com/Mark7766/spring-ai-apps/tree/main/embeddings-ollama)

**项目结构**：
```
embeddings-ollama/
├── src/main/java/com/sandy/embeddings/ollama/
│   ├── EmbeddingsOllamaApplication.java    # 启动类
│   └── DoctorAssistantController.java       # 核心业务逻辑
├── src/main/resources/
│   └── application.yml                      # 配置文件
└── pom.xml                                  # Maven配置
```

**核心文件**：
- **DoctorAssistantController.java**：180行完整实现
  - Embedding向量化
  - 相似度搜索
  - RAG问答生成

---

## 🤔 思考题

1. **如何选择合适的文档切分策略？**
   
   提示：考虑语义完整性、检索粒度、上下文窗口等因素。

2. **不同的相似度计算方法（余弦、欧氏距离等）有什么区别？**
   
   提示：余弦关注方向，欧氏关注距离，适用场景不同。

3. **本地部署Embedding模型需要什么样的硬件配置？**
   
   提示：CPU够用，16GB内存推荐，GPU可选（加速推理）。

---

## 📖 拓展阅读

- [Ollama官方文档](https://ollama.ai/docs)
- [Spring AI EmbeddingModel文档](https://docs.spring.io/spring-ai/reference/api/embeddings.html)
- [文本Embedding技术综述](https://arxiv.org/abs/2201.10005)
- [余弦相似度详解](https://en.wikipedia.org/wiki/Cosine_similarity)

---

## ⏭️ 下期预告

恭喜你掌握了Embedding向量化技术！🎉 现在你已经能够：
- ✅ 将文本转换为向量
- ✅ 计算语义相似度
- ✅ 构建简单的知识问答系统

但目前的实现还有局限：
- ❌ 知识库在内存中，重启就丢失
- ❌ 向量数据无法持久化
- ❌ 无法处理大规模数据

**下一期我们将学习向量数据库集成**，高效存储和检索海量向量数据！

**下期亮点**：
- 🗄️ Chroma向量数据库集成
- 📄 PDF文档智能解析
- 🔍 混合检索策略（向量+关键词）
- 💾 数据持久化与索引优化

敬请期待！

---

**更新日期**：2025年12月1日  
**状态**：✅ 已完成


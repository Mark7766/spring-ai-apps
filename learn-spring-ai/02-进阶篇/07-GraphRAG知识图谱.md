# 第7期：GraphRAG知识图谱 - 让AI理解数据之间的关系网络

## 📌 本期概述

**核心问题：如何让AI理解数据之间的关系网络？**

传统RAG只能检索相似文本片段，无法理解实体间的复杂关系。GraphRAG通过知识图谱技术，让AI能够理解"谁认识谁"、"谁在哪工作"等关系网络，实现基于关系的多跳推理。本期将深入讲解Neo4j图数据库集成，构建企业组织关系知识图谱。

## 🎯 学习目标

完成本期学习后，你将能够：
- ✅ 理解GraphRAG相比传统RAG的优势
- ✅ 掌握Neo4j图数据库基础和Cypher查询语言
- ✅ 集成Neo4j到Spring AI应用
- ✅ 构建知识图谱并实现图谱查询
- ✅ 实现基于关系的多跳推理问答
- ✅ 应对复杂关系场景的问答需求

## 📚 内容大纲

### 1. 传统RAG的局限性

### 2. GraphRAG技术原理

### 3. Neo4j图数据库入门

### 4. Spring AI集成Neo4j

### 5. 知识图谱构建与查询

### 6. 复杂关系推理实战

---

## 1. 传统RAG的局限性

### 1.1 传统RAG的问题

**场景**：企业组织关系问答

```
问题1："xAI公司有哪些员工？"
传统RAG：✅ 可以回答（检索文档中的员工列表）

问题2："Elon Musk领导的所有公司的员工总数？"
传统RAG：❌ 难以回答
原因：
1. 需要知道Elon Musk领导哪些公司（关系推理）
2. 需要汇总多个公司的员工（多跳查询）
3. 文档片段无法表达这种网络关系
```

**传统RAG的4个局限**：

| 局限 | 说明 | 示例 |
|------|------|------|
| **关系理解弱** | 无法理解实体间的连接 | "A的老板的同事是谁？" |
| **多跳推理难** | 无法进行多步关系推导 | "朋友的朋友推荐的书" |
| **结构化查询差** | 难以处理复杂的查询逻辑 | "同时在两家公司工作的人" |
| **关系遍历慢** | 需要多次检索和拼接 | "找出所有二度人脉" |

### 1.2 GraphRAG的解决方案

**GraphRAG = 图谱检索 + 生成**

```
知识图谱：
┌─────────────────────────────────────┐
│  (Elon Musk)                        │
│      │                              │
│      ├─[CEO]→ (Tesla)               │
│      │          └─[employs]→ (Alice)│
│      └─[CEO]→ (xAI)                 │
│                 └─[employs]→ (Bob)  │
└─────────────────────────────────────┘

问题："Elon Musk领导的公司有哪些员工？"
GraphRAG流程：
1. 识别实体：Elon Musk
2. 图谱查询：MATCH (p:Person {name:"Elon Musk"})-[:CEO]->(c:Company)-[:employs]->(e:Employee)
3. 获取结果：Alice, Bob
4. LLM生成答案："Elon Musk领导的公司员工包括Tesla的Alice和xAI的Bob"
```

**GraphRAG的4大优势**：

```
✅ 关系推理：理解多跳关系链
✅ 结构化查询：精确的图谱遍历
✅ 高效检索：图数据库优化的查询
✅ 可解释性：清晰的推理路径
```

---

## 2. GraphRAG技术原理

### 2.1 知识图谱基础

**知识图谱三要素**：

```
节点（Node）：实体
  例：(Elon Musk), (Tesla), (xAI)

关系（Relationship）：连接
  例：[CEO], [employs], [located_in]

属性（Property）：附加信息
  例：{name: "Elon Musk", age: 52}
```

**示例图谱**：

```
(Elon Musk:Person {name:"Elon Musk", age:52})
    │
    ├─[:CEO {since:2008}]→ (Tesla:Company {name:"Tesla", founded:2003})
    │                          └─[:employs]→ (Alice:Employee {name:"Alice", role:"Engineer"})
    │
    └─[:CEO {since:2023}]→ (xAI:Company {name:"xAI", founded:2023})
                               └─[:employs]→ (Bob:Employee {name:"Bob", role:"Researcher"})
```

### 2.2 GraphRAG工作流程

**完整流程**：

```
【离线阶段：图谱构建】
1. 实体抽取：从文档识别实体（人、公司、地点等）
2. 关系抽取：识别实体间的关系
3. 图谱构建：将实体和关系存入Neo4j
4. 向量化：为节点生成Embedding（可选）

【在线阶段：问答】
1. 问题理解：解析用户问题，识别关键实体
2. 图谱查询：构建Cypher查询，检索相关子图
3. 上下文构建：将图谱数据格式化为文本
4. LLM生成：基于图谱上下文生成答案
5. 返回结果
```

**可视化流程**：

```
用户问题："xAI公司的员工有谁？"
    ↓
【实体识别】
  识别：xAI（公司）
    ↓
【图谱查询】Cypher
  MATCH (c:Company {name:"xAI"})-[:employs]->(e:Employee)
  RETURN e.name, e.role
    ↓
【获取结果】
  Bob, Researcher
    ↓
【构建上下文】
  "员工：Bob，角色：Researcher，公司：xAI"
    ↓
【LLM生成】
  "xAI公司的员工是Bob，他是一名研究员。"
```

### 2.3 GraphRAG vs 传统RAG对比

| 维度 | 传统RAG | GraphRAG |
|------|---------|----------|
| **数据结构** | 扁平文本片段 | 图结构（节点+关系） |
| **检索方式** | 向量相似度 | 图遍历+向量 |
| **关系理解** | 弱 | 强 |
| **多跳推理** | 困难 | 天然支持 |
| **查询精度** | 依赖相似度 | 精确匹配 |
| **适用场景** | 文档问答 | 关系问答、推理 |
| **构建成本** | 低 | 中高（需要实体/关系抽取） |

---

## 3. Neo4j图数据库入门

### 3.1 什么是Neo4j？

**Neo4j** 是世界上最流行的图数据库，原生支持图存储和图查询。

**特点**：
- 🎯 原生图存储（不是关系型数据库模拟）
- ⚡ 高效图遍历（百万级节点毫秒级查询）
- 🔍 Cypher查询语言（类SQL，易学）
- 🌐 支持分布式和集群
- 📊 强大的可视化工具

### 3.2 安装Neo4j

**方式1：Docker（推荐）**

```bash
# 拉取Neo4j镜像
docker pull neo4j:latest

# 启动Neo4j
docker run -d \
  --name neo4j \
  -p 7474:7474 \
  -p 7687:7687 \
  -e NEO4J_AUTH=neo4j/your_password \
  neo4j:latest

# 访问Web界面
# http://localhost:7474
# 默认用户名：neo4j
# 首次登录需要修改密码
```

**方式2：桌面版**

1. 访问 [https://neo4j.com/download/](https://neo4j.com/download/)
2. 下载Neo4j Desktop
3. 安装并启动
4. 创建数据库，设置密码

**验证安装**：

```bash
# 浏览器访问
http://localhost:7474

# 输入用户名密码
用户名：neo4j
密码：your_password

# 执行测试查询
MATCH (n) RETURN count(n)
```

### 3.3 Cypher查询语言速成

**Cypher** 是Neo4j的查询语言，类似SQL但专为图设计。

**基本语法**：

```cypher
// 1. 创建节点
CREATE (p:Person {name: "Elon Musk", age: 52})

// 2. 创建关系
MATCH (p:Person {name: "Elon Musk"}), (c:Company {name: "Tesla"})
CREATE (p)-[:CEO {since: 2008}]->(c)

// 3. 查询节点
MATCH (p:Person {name: "Elon Musk"})
RETURN p

// 4. 查询关系
MATCH (p:Person)-[:CEO]->(c:Company)
RETURN p.name, c.name

// 5. 多跳查询
MATCH (p:Person)-[:CEO]->(c:Company)-[:employs]->(e:Employee)
RETURN p.name, c.name, e.name

// 6. 删除
MATCH (n) DELETE n  // 删除所有节点
```

**常用模式**：

```cypher
// 模式1：单向关系
(a)-[:KNOWS]->(b)

// 模式2：双向关系
(a)-[:FRIENDS]-(b)

// 模式3：多跳路径
(a)-[:KNOWS*1..3]->(b)  // 1到3跳

// 模式4：条件过滤
MATCH (p:Person)
WHERE p.age > 30
RETURN p
```

### 3.4 初始化示例数据

在Neo4j浏览器中执行以下Cypher脚本：

```cypher
// 清空数据库
MATCH (n) DETACH DELETE n;

// 创建人物
CREATE (elon:Person {name: "Elon Musk", age: 52});

// 创建公司
CREATE (tesla:Company {name: "Tesla", founded: 2003});
CREATE (xai:Company {name: "xAI", founded: 2023});

// 创建员工
CREATE (alice:Employee {name: "Alice", role: "Engineer"});
CREATE (bob:Employee {name: "Bob", role: "Researcher"});

// 创建关系
MATCH (elon:Person {name: "Elon Musk"}), (tesla:Company {name: "Tesla"})
CREATE (elon)-[:CEO {since: 2008}]->(tesla);

MATCH (elon:Person {name: "Elon Musk"}), (xai:Company {name: "xAI"})
CREATE (elon)-[:CEO {since: 2023}]->(xai);

MATCH (tesla:Company {name: "Tesla"}), (alice:Employee {name: "Alice"})
CREATE (tesla)-[:employs]->(alice);

MATCH (xai:Company {name: "xAI"}), (bob:Employee {name: "Bob"})
CREATE (xai)-[:employs]->(bob);

// 验证数据
MATCH (n) RETURN n LIMIT 25;
```

**执行后的图谱结构**：

```
(Elon Musk:Person)
    ├─[:CEO]→ (Tesla:Company) ─[:employs]→ (Alice:Employee)
    └─[:CEO]→ (xAI:Company) ─[:employs]→ (Bob:Employee)
```

---

## 4. Spring AI集成Neo4j

现在开始构建GraphRAG应用！

项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/neo4j-ollama](https://github.com/Mark7766/spring-ai-apps/tree/main/neo4j-ollama)

### 4.1 项目依赖配置

```xml
<!-- 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/neo4j-ollama/pom.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.4</version>
    </parent>
    
    <groupId>com.sandy</groupId>
    <artifactId>neo4j-ollama</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    
    <properties>
        <java.version>17</java.version>
        <spring-ai.version>1.0.0</spring-ai.version>
    </properties>
    
    <dependencies>
        <!-- ⭐ Spring Data Neo4j（图数据库ORM） -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-neo4j</artifactId>
        </dependency>
        
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- ⭐ Spring AI Neo4j向量存储（可选） -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-vector-store-neo4j</artifactId>
        </dependency>
        
        <!-- Spring AI Ollama -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-starter-model-ollama</artifactId>
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

1. **spring-boot-starter-data-neo4j**：Neo4j集成，提供Neo4jClient
2. **spring-ai-starter-vector-store-neo4j**：Neo4j向量存储（可选，用于混合检索）
3. **spring-ai-starter-model-ollama**：本地Ollama模型

### 4.2 应用配置

```yaml
# 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/neo4j-ollama/src/main/resources/application.yml
server:
  port: 8081

spring:
  application:
    name: "neo4j-ollama"
  
  # ⭐ Neo4j连接配置
  neo4j:
    uri: bolt://localhost:7687  # Neo4j Bolt协议地址
    authentication:
      username: neo4j
      password: your_password    # 你设置的密码
  
  # Ollama配置
  ai:
    ollama:
      base-url: "http://localhost:11434"
      embedding:
        enabled: true
        model: qwen2.5
      chat:
        enabled: true
        model: qwen2.5
```

**配置说明**：
- `neo4j.uri`：Neo4j连接地址，Bolt协议端口7687
- `neo4j.authentication`：认证信息
- `ollama`：本地LLM配置

---

## 5. 知识图谱构建与查询

### 5.1 图谱查询服务

创建`EmployeeService.java`，实现图谱查询：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/neo4j-ollama/src/main/java/com/sandy/neo4j/ollama/EmployeeService.java
package com.sandy.neo4j.ollama;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

@Service
public class EmployeeService {
    
    @Autowired
    private Neo4jClient neo4jClient;

    /**
     * ⭐ 根据公司名查询员工
     * Cypher: MATCH (e:Employee)-[:WORKS_FOR]->(c:Company {name: $companyName})
     *         RETURN e.name, e.role, c.name
     */
    public Collection<Map<String, Object>> findEmployeeByCompany(String companyName) {
        String query = """
            MATCH (e:Employee)-[:WORKS_FOR]->(c:Company {name: $companyName})
            RETURN e.name AS employeeName, e.role AS role, c.name AS companyName
            """;
        
        return neo4jClient
            .query(query)
            .bind(companyName).to("companyName")  // 绑定参数
            .fetch()
            .all();  // 返回所有结果
    }
}
```

**代码核心解析**：

1. **Neo4jClient注入**：
   ```java
   @Autowired
   private Neo4jClient neo4jClient;
   ```
   Spring自动配置的Neo4j客户端。

2. **Cypher查询**：
   ```cypher
   MATCH (e:Employee)-[:WORKS_FOR]->(c:Company {name: $companyName})
   RETURN e.name AS employeeName, e.role AS role, c.name AS companyName
   ```
   匹配模式：员工 ─[工作于]→ 公司

3. **参数绑定**：
   ```java
   .bind(companyName).to("companyName")
   ```
   防止Cypher注入，类似SQL的PreparedStatement。

### 5.2 扩展图谱查询

添加更多图谱查询方法：

```java
// 继续在EmployeeService中添加

/**
 * ⭐ 查询某人领导的所有公司
 */
public Collection<Map<String, Object>> findCompaniesByLeader(String leaderName) {
    String query = """
        MATCH (p:Person {name: $leaderName})-[:CEO]->(c:Company)
        RETURN c.name AS companyName, c.founded AS founded
        """;
    
    return neo4jClient
        .query(query)
        .bind(leaderName).to("leaderName")
        .fetch()
        .all();
}

/**
 * ⭐ 多跳查询：某人领导的公司的所有员工
 */
public Collection<Map<String, Object>> findEmployeesByLeader(String leaderName) {
    String query = """
        MATCH (p:Person {name: $leaderName})-[:CEO]->(c:Company)-[:employs]->(e:Employee)
        RETURN p.name AS leaderName, c.name AS companyName, 
               e.name AS employeeName, e.role AS role
        """;
    
    return neo4jClient
        .query(query)
        .bind(leaderName).to("leaderName")
        .fetch()
        .all();
}

/**
 * ⭐ 统计查询：每个公司的员工数
 */
public Collection<Map<String, Object>> countEmployeesByCompany() {
    String query = """
        MATCH (c:Company)-[:employs]->(e:Employee)
        RETURN c.name AS companyName, count(e) AS employeeCount
        ORDER BY employeeCount DESC
        """;
    
    return neo4jClient
        .query(query)
        .fetch()
        .all();
}
```

---

## 6. 复杂关系推理实战

### 6.1 GraphRAG问答控制器

创建`AIController.java`，实现GraphRAG问答：

```java
// 来自：https://github.com/Mark7766/spring-ai-apps/blob/main/neo4j-ollama/src/main/java/com/sandy/neo4j/ollama/AIController.java
package com.sandy.neo4j.ollama;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AIController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private ChatModel chatModel;

    /**
     * ⭐ GraphRAG问答接口
     */
    @GetMapping("/ask")
    public String askQuestion(@RequestParam String question) {
        // ⭐ Step 1: 从问题中提取关键实体（简单示例）
        String companyName = extractCompanyName(question);
        
        // ⭐ Step 2: 图谱查询，获取相关上下文
        Collection<Map<String, Object>> context = 
            employeeService.findEmployeeByCompany(companyName);
        
        // ⭐ Step 3: 格式化图谱数据为文本上下文
        String contextString = formatContext(context);
        
        // ⭐ Step 4: 构建RAG Prompt
        String prompt = "基于以下信息回答问题:\n" 
                      + contextString 
                      + "\n问题: " + question;
        
        System.out.println("prompt:" + prompt);
        
        // ⭐ Step 5: LLM生成答案
        ChatResponse chatResponse = chatModel.call(new Prompt(prompt));
        return chatResponse.getResult().getOutput().getText();
    }

    /**
     * 格式化图谱上下文
     */
    private String formatContext(Collection<Map<String, Object>> context) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> entry : context) {
            sb.append("员工: ").append(entry.get("employeeName"))
              .append(", 角色: ").append(entry.get("role"))
              .append(", 公司: ").append(entry.get("companyName"))
              .append("\n");
        }
        return sb.toString();
    }

    /**
     * 简单的实体抽取（实际应用应使用NER模型）
     */
    private String extractCompanyName(String question) {
        // 关键词匹配（简化示例）
        if (question.contains("xAI")) return "xAI";
        if (question.contains("Tesla")) return "Tesla";
        
        // 默认值
        return "xAI";
    }
}
```

**GraphRAG完整流程**：

```
用户问题："xAI公司有哪些员工？"
    ↓
【Step 1】实体抽取
  识别：xAI（公司名）
    ↓
【Step 2】图谱查询
  Cypher: MATCH (e:Employee)-[:WORKS_FOR]->(c:Company {name:"xAI"})
          RETURN e.name, e.role, c.name
  结果：Bob, Researcher, xAI
    ↓
【Step 3】格式化上下文
  "员工：Bob，角色：Researcher，公司：xAI"
    ↓
【Step 4】构建Prompt
  "基于以下信息回答问题：
   员工：Bob，角色：Researcher，公司：xAI
   问题：xAI公司有哪些员工？"
    ↓
【Step 5】LLM生成
  "xAI公司的员工是Bob，他是一名研究员。"
```

### 6.2 多跳推理实现

扩展控制器，支持多跳推理：

```java
// 继续在AIController中添加

/**
 * ⭐ 多跳推理问答
 * 示例："Elon Musk领导的公司有哪些员工？"
 */
@GetMapping("/ask-multi-hop")
public String askMultiHop(@RequestParam String question) {
    // Step 1: 识别查询类型和实体
    String leaderName = extractLeaderName(question);
    
    // Step 2: 多跳图谱查询
    Collection<Map<String, Object>> context = 
        employeeService.findEmployeesByLeader(leaderName);
    
    // Step 3: 格式化为结构化上下文
    String contextString = formatMultiHopContext(context);
    
    // Step 4: 构建增强Prompt
    String prompt = String.format("""
        你是一个企业知识图谱问答助手。
        
        基于以下组织关系信息回答问题：
        %s
        
        用户问题：%s
        
        请基于提供的信息给出准确的答案，如果信息不足，请说明。
        """, contextString, question);
    
    // Step 5: LLM生成
    ChatResponse response = chatModel.call(new Prompt(prompt));
    return response.getResult().getOutput().getText();
}

/**
 * 格式化多跳查询结果
 */
private String formatMultiHopContext(Collection<Map<String, Object>> context) {
    StringBuilder sb = new StringBuilder();
    
    // 按公司分组
    Map<String, List<String>> companyEmployees = new HashMap<>();
    for (Map<String, Object> entry : context) {
        String company = (String) entry.get("companyName");
        String employee = String.format("%s (%s)", 
            entry.get("employeeName"), 
            entry.get("role"));
        
        companyEmployees
            .computeIfAbsent(company, k -> new ArrayList<>())
            .add(employee);
    }
    
    // 格式化输出
    companyEmployees.forEach((company, employees) -> {
        sb.append(String.format("公司：%s\n", company));
        sb.append(String.format("  员工：%s\n", String.join(", ", employees)));
    });
    
    return sb.toString();
}

/**
 * 提取领导者名字
 */
private String extractLeaderName(String question) {
    if (question.contains("Elon Musk") || question.contains("马斯克")) {
        return "Elon Musk";
    }
    return "Elon Musk";  // 默认
}
```

### 6.3 测试GraphRAG系统

**启动应用**：

```bash
# 1. 确保Neo4j运行
docker ps | grep neo4j

# 2. 确保Ollama运行
ollama list

# 3. 启动Spring Boot应用
cd neo4j-ollama
mvn spring-boot:run
```

**测试单跳查询**：

```bash
curl "http://localhost:8081/api/ask?question=xAI公司有哪些员工"

# 返回：
# "xAI公司的员工是Bob，他是一名研究员。"
```

**测试多跳推理**：

```bash
curl "http://localhost:8081/api/ask-multi-hop?question=Elon%20Musk领导的公司有哪些员工"

# 返回：
# "Elon Musk领导的公司包括Tesla和xAI。
#  Tesla的员工有Alice（工程师），
#  xAI的员工有Bob（研究员）。"
```

### 6.4 复杂场景示例

**场景1：统计聚合**

```java
@GetMapping("/company-stats")
public String getCompanyStats() {
    Collection<Map<String, Object>> stats = 
        employeeService.countEmployeesByCompany();
    
    StringBuilder context = new StringBuilder("公司员工统计：\n");
    stats.forEach(entry -> {
        context.append(String.format("- %s: %d人\n", 
            entry.get("companyName"), 
            entry.get("employeeCount")));
    });
    
    String prompt = context + "\n请总结这些公司的规模情况。";
    
    return chatModel.call(new Prompt(prompt))
        .getResult().getOutput().getText();
}
```

**场景2：关系推荐**

```java
/**
 * 基于图谱的推荐
 * "推荐与Alice同公司的其他员工"
 */
@GetMapping("/recommend")
public String recommend(@RequestParam String employeeName) {
    String query = """
        MATCH (e1:Employee {name: $employeeName})<-[:employs]-(c:Company)-[:employs]->(e2:Employee)
        WHERE e1 <> e2
        RETURN e2.name AS name, e2.role AS role
        """;
    
    Collection<Map<String, Object>> colleagues = neo4jClient
        .query(query)
        .bind(employeeName).to("employeeName")
        .fetch()
        .all();
    
    // 构建推荐上下文
    String context = formatContext(colleagues);
    String prompt = String.format(
        "为%s推荐同公司的同事，以下是候选人：\n%s", 
        employeeName, context);
    
    return chatModel.call(new Prompt(prompt))
        .getResult().getOutput().getText();
}
```

---

## 💻 示例代码

完整项目代码：[https://github.com/Mark7766/spring-ai-apps/tree/main/neo4j-ollama](https://github.com/Mark7766/spring-ai-apps/tree/main/neo4j-ollama)

**项目结构**：
```
neo4j-ollama/
├── src/main/java/com/sandy/neo4j/ollama/
│   ├── Neo4jOllamaApplication.java    # 启动类
│   ├── EmployeeService.java           # 图谱查询服务
│   └── AIController.java              # GraphRAG问答API
├── src/main/resources/
│   └── application.yml                # 配置文件
└── pom.xml
```

**核心文件**：
- **EmployeeService.java**：封装Cypher查询逻辑
- **AIController.java**：实现GraphRAG完整流程

---

## 🤔 思考题

1. **如何从非结构化文本中自动抽取实体和关系？**
   
   提示：可以使用NER（命名实体识别）模型、关系抽取模型，或让LLM帮助抽取。

2. **GraphRAG在什么场景下优于传统RAG？**
   
   提示：复杂关系网络、多跳推理、结构化知识、推荐系统等场景。

3. **如何平衡图谱构建成本和问答质量提升？**
   
   提示：可以混合使用，重要关系用图谱，一般内容用向量检索。

---

## 📖 拓展阅读

- [Neo4j官方文档](https://neo4j.com/docs/)
- [Cypher查询语言手册](https://neo4j.com/docs/cypher-manual/)
- [GraphRAG技术论文](https://arxiv.org/abs/2404.16130)
- [Spring Data Neo4j文档](https://docs.spring.io/spring-data/neo4j/reference/)

---

## ⏭️ 下期预告

恭喜你掌握了GraphRAG知识图谱技术！🎉 现在你已经能够：
- ✅ 构建知识图谱
- ✅ 使用Cypher查询图数据库
- ✅ 实现基于关系的多跳推理
- ✅ 应对复杂的关系问答场景

**进阶篇到此结束！** 下一期进入**高级篇**，我们将学习**Function Calling工具调用**，让AI主动调用你的工具和API！

**下期亮点**：
- 🛠️ Function Calling原理
- 📞 工具定义与注册
- 🔧 天气查询、数据库操作等实战
- 🤖 让AI成为你的智能助手

敬请期待！

---

**更新日期**：2025年12月3日  
**状态**：✅ 已完成


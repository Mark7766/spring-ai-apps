# Chat OCR - AI多模态图像识别聊天应用

这是一个基于Spring Boot 3.5.4和Spring AI 1.0.0的企业级多模态图像识别聊天应用，使用Ollama的视觉语言模型（如LLaVA、Gemma3等）进行图像识别和智能对话。

## 功能特性

- 📸 **多图片上传与识别**: 支持单次上传多张图片并进行OCR识别和分析
- 💬 **多轮对话交互**: 支持连续的问答交互，带完整的会话管理功能(目前不带上下文记忆，后续有需要可以加)
- 🎯 **场景化应用**: 内置三大应用场景（设计图纸审核、医学影像咨询、设备故障诊断）
- 🎨 **现代化UI**: 参照X.com(Twitter)设计的深色/浅色双主题界面
- 📋 **多种上传方式**: 支持拖拽、粘贴、点击上传图片
- 💾 **会话持久化**: 自动保存会话记录和图片到本地文件系统
- ⚡ **实时响应**: 流畅的对话体验、打字动画和加载指示器
- 🔄 **会话管理**: 支持创建、切换、删除多个会话
- 🎭 **快捷操作**: 每个场景提供特定的快捷操作按钮

## 技术栈

- **后端**:
  - Spring Boot 3.5.4
  - Spring AI 1.0.0 (Ollama集成)
  - Spring Boot Actuator (监控和健康检查)
  - Lombok (简化Java开发)
  - Jackson (JSON处理和序列化)
  - Thumbnailator 0.4.20 (图片处理和压缩)

- **前端**:
  - Thymeleaf (服务端模板引擎)
  - 原生JavaScript (ES6+)
  - CSS3 (深色/浅色主题)
  - 响应式设计

- **存储**:
  - 本地文件系统 (会话和图片持久化)
  - JSON文件存储 (会话数据和消息记录)

## 前置要求

1. **Java 17或更高版本**
2. **Maven 3.6+**
3. **Ollama**: 需要在本地安装并运行Ollama

### 安装Ollama和模型

```bash
# 1. 访问 https://ollama.ai 下载并安装Ollama

# 2. 拉取支持视觉的多模态模型
ollama pull gemma3:12b

# 或者使用其他视觉模型:
ollama pull llava           # LLaVA视觉语言模型
ollama pull llava:13b       # 更大更强的模型
ollama pull bakllava        # 另一个视觉模型选择

# 查看已安装的模型
ollama list
```

**注意**: 本项目默认使用 `gemma3:12b` 模型，这是一个支持多模态（文本+图像）的高性能模型。

## 快速开始

### 1. 克隆项目

```bash
cd spring-ai-apps/chat-ocr
```

### 2. 配置应用

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: chat-ocr
  
  # Ollama AI 配置
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: gemma3:12b  # 使用的模型
          temperature: 0.7   # 控制回答的随机性 (0.0-1.0)
  
  # 文件上传配置
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 50MB

# 服务器配置
server:
  port: 8080

# 聊天存储配置
chat:
  storage-path: ${user.dir}/data  # 数据存储路径
  
  # 场景配置
  scenarios:
    design:
      name: 设计图纸审核
      icon: 📋
      system-prompt: 你是一位专业的工程设计审查专家...
      quick-actions: 尺寸核对,规范检查,BOM清单
    
    medical:
      name: 医疗影像问诊
      icon: 🏥
      system-prompt: 你是一位医学影像分析助手...
      quick-actions: 病灶识别,测量计算,历史对比
    
    equipment:
      name: 设备故障诊断
      icon: ⚙️
      system-prompt: 你是一位设备维修专家...
      quick-actions: 故障定位,配件识别,维修建议
```

### 3. 启动Ollama服务

```bash
ollama serve
```

### 4. 运行应用

**方式一: 使用Maven(推荐开发)**
```bash
mvn spring-boot:run
```

**方式二: 运行打包后的JAR(推荐生产)**
```bash
mvn clean package
java -jar target/chat-ocr-1.0.0-rc1.jar
```

### 5. 访问应用

打开浏览器访问: http://localhost:8080

## 使用方法

### 基本操作

1. **选择场景**: 在欢迎页面选择应用场景（设计图纸审核、医学影像咨询、设备故障诊断）
2. **创建会话**: 点击"新建会话"按钮创建新的聊天会话
3. **纯文本对话**: 直接在输入框输入问题，点击发送
4. **上传图片分析**: 
   - 点击图片图标选择文件（支持多选）
   - 或者直接拖拽图片到输入框
   - 或者粘贴图片到输入框(Ctrl+V)
5. **快捷操作**: 点击快捷按钮快速发送预设的常用问题
6. **会话管理**: 
   - 在左侧会话列表切换不同会话
   - 点击删除按钮删除不需要的会话
   - 每个会话显示消息数和图片数

### 三大应用场景

#### 1. 设计图纸审核 📋
专业的工程设计审查专家，可以：
- 尺寸核对：检查图纸上的尺寸标注
- 规范检查：验证设计是否符合规范
- BOM清单：识别和生成物料清单

#### 2. 医学影像咨询 🏥
医学影像分析助手，可以：
- 病灶识别：识别影像中的异常区域
- 测量计算：计算病灶大小和其他参数
- 历史对比：对比不同时期的影像变化

#### 3. 设备故障诊断 ⚙️
设备维修专家，可以：
- 故障定位：识别设备故障位置
- 配件识别：识别需要更换的配件
- 维修建议：提供详细的维修方案

## 项目结构

```
chat-ocr/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sandy/chat/ocr/
│   │   │       ├── ChatOcrApplication.java        # 主应用类
│   │   │       ├── config/
│   │   │       │   ├── JacksonConfig.java         # Jackson配置
│   │   │       │   └── ScenarioProperties.java    # 场景配置属性
│   │   │       ├── controller/
│   │   │       │   ├── WebController.java         # Web页面控制器
│   │   │       │   ├── ChatController.java        # 聊天API控制器
│   │   │       │   ├── SessionController.java     # 会话管理控制器
│   │   │       │   └── ImageController.java       # 图片访问控制器
│   │   │       ├── service/
│   │   │       │   ├── OcrChatService.java        # 核心聊天服务
│   │   │       │   ├── SessionService.java        # 会话管理服务
│   │   │       │   ├── MessageService.java        # 消息管理服务
│   │   │       │   └── FileStorageService.java    # 文件存储服务
│   │   │       ├── model/
│   │   │       │   ├── Message.java               # 消息模型
│   │   │       │   ├── Session.java               # 会话模型
│   │   │       │   ├── Scenario.java              # 场景枚举
│   │   │       │   ├── MessageRole.java           # 消息角色枚举
│   │   │       │   └── MessageStatus.java         # 消息状态枚举
│   │   │       ├── dto/
│   │   │       │   ├── ChatRequest.java           # 请求DTO
│   │   │       │   └── ChatResponse.java          # 响应DTO
│   │   │       └── util/
│   │   │           └── JsonFileStore.java         # JSON文件存储工具
│   │   └── resources/
│   │       ├── application.yml                    # 应用配置文件
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── chat.css                   # 聊天界面样式
│   │       │   └── js/
│   │       │       └── chat.js                    # 聊天交互逻辑
│   │       └── templates/
│   │           ├── welcome.html                   # 欢迎页面
│   │           └── chat.html                      # 聊天界面
│   └── test/
│       └── java/
│           └── com/sandy/chat/ocr/
│               └── ChatOcrApplicationTests.java   # 单元测试
├── data/                                          # 数据存储目录
│   └── sessions/                                  # 会话数据
│       ├── session-{id}.json                      # 会话元数据
│       └── session-{id}/                          # 会话目录
│           ├── messages.json                      # 消息记录
│           └── images/                            # 图片存储
├── docs/                                          # 文档目录
│   ├── article-1-drawing-review.md               # 设计图纸审核文章
│   ├── article-2-device-diagnosis.md             # 设备故障诊断文章
│   └── article-3-java-multimodal-spring-ai.md   # Spring AI多模态文章
├── pom.xml                                        # Maven配置
└── README.md                                      # 项目说明文档
```

## API接口

### POST /api/chat/{sessionId}/message

发送消息并上传图片（支持多图片）

**请求参数**:
- `sessionId` (Path): 会话ID
- `message` (FormData): 用户消息
- `images` (FormData, 可选): 图片文件列表

**响应**:
```json
{
  "message": "AI的回复内容",
  "success": true
}
```

### GET /api/chat/{sessionId}/messages

获取会话消息列表

**请求参数**:
- `sessionId` (Path): 会话ID
- `limit` (Query, 可选): 消息数量限制，默认50

**响应**:
```json
{
  "success": true,
  "messages": [
    {
      "id": "消息ID",
      "role": "USER/ASSISTANT",
      "content": "消息内容",
      "imagePaths": ["图片路径"],
      "timestamp": "2024-12-05T10:00:00"
    }
  ]
}
```

### POST /api/sessions

创建新会话

**请求参数**:
- `scenario` (JSON): 场景类型（DESIGN/MEDICAL/EQUIPMENT）

**响应**:
```json
{
  "success": true,
  "sessionId": "会话ID"
}
```

### GET /api/sessions

获取所有会话列表

**响应**:
```json
{
  "success": true,
  "sessions": [
    {
      "id": "会话ID",
      "name": "会话名称",
      "scenario": "DESIGN",
      "messageCount": 10,
      "imageCount": 3,
      "createdAt": "2024-12-05T10:00:00"
    }
  ]
}
```

### DELETE /api/sessions/{sessionId}

删除会话

**请求参数**:
- `sessionId` (Path): 会话ID

**响应**:
```json
{
  "success": true
}
```

## 配置说明

### Ollama模型配置

可以在 `application.yml` 中修改使用的模型:

```yaml
spring:
  ai:
    ollama:
      chat:
        options:
          model: gemma3:12b  # 修改为你想使用的模型
```

支持的视觉模型:
- `gemma3:12b` - Gemma3多模态模型，12B参数(默认，推荐)
- `llava` - 默认的视觉语言模型
- `llava:13b` - 13B参数的大模型(更强但需要更多资源)
- `bakllava` - 另一个视觉模型选择

**注意**: 必须使用支持视觉能力的模型才能进行图像识别。文本模型如 `deepseek-r1` 不支持图像输入。

### 文件上传限制

```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB      # 单个文件最大10MB
      max-request-size: 50MB   # 请求最大50MB(支持多图片)
```

### 数据存储路径

```yaml
chat:
  storage-path: ${user.dir}/data  # 默认使用项目目录下的data文件夹
```

会话数据和图片文件将存储在此路径下的sessions目录中。

## 常见问题

### 1. 无法连接到Ollama

确保Ollama服务正在运行:
```bash
ollama serve
```

检查端口是否正确: http://localhost:11434

### 2. 图片上传失败

- 检查图片大小是否超过10MB
- 确认图片格式是否支持(PNG, JPG, JPEG等)
- 检查文件上传配置是否正确

### 3. OCR识别不准确

- 尝试使用更高质量的图片
- 使用更大的模型如 `llava:13b`
- 调整temperature参数(0.0-1.0,越低越确定)

### 4. 模型未找到错误

如果出现模型未找到的错误,请先拉取模型:
```bash
ollama pull gemma3:12b
```

查看已安装的模型:
```bash
ollama list
```

### 5. 会话数据丢失

检查 `chat.storage-path` 配置是否正确，确保应用有写入权限。会话数据保存在 `${user.dir}/data/sessions` 目录下。

### 6. 内存不足

大型模型如 `llava:13b` 或 `gemma3:12b` 需要较多内存。如果遇到内存不足，可以：
- 使用较小的模型如 `llava`
- 增加Java堆内存: `java -Xmx4g -jar chat-ocr-1.0.0-rc1.jar`
- 使用GPU加速（如果支持）

## 界面预览

应用采用现代化的深色/浅色双主题设计,参考X.com(Twitter)的UI风格:

- **欢迎页面**: 渐变背景，场景卡片选择
- **聊天界面**: 
  - 左侧会话列表，支持场景切换
  - 中间消息区域，支持文本和图片显示
  - 右侧快捷操作面板
- **深色/浅色主题**: 一键切换，舒适护眼
- **流畅动画效果**: 消息加载、打字指示器
- **响应式设计**: 支持桌面和移动端
- **直观的图片上传**: 支持拖拽、粘贴、点击
- **实时打字指示器**: 显示AI正在思考

## 开发调试

启用调试日志，编辑 `application.yml`:

```yaml
logging:
  level:
    com.sandy.chat.ocr: DEBUG           # 应用日志
    org.springframework.ai: DEBUG       # Spring AI日志
```

## 性能优化建议

1. **GPU加速**: Ollama支持GPU加速,可显著提升性能
2. **模型选择**: 根据硬件配置选择合适的模型大小
3. **并发控制**: 在生产环境中添加请求队列和并发限制
4. **缓存**: 对相同图片的识别结果可以考虑缓存

## 未来如果有需要，建议扩展的方向如下:

- 🔮 **上下文记忆**: 支持会话级别的上下文记忆，实现真正的多轮对话连续性
- 📊 **统计分析**: 添加使用统计和分析功能
- 🔐 **用户认证**: 实现多用户支持和权限管理
- 🌐 **多语言支持**: 支持国际化和多语言界面
- 📤 **导出功能**: 支持导出会话记录为PDF或Word文档
- 🎤 **语音交互**: 支持语音输入和语音回复

## 许可证

本项目遵循MIT许可证

## 作者

Sandy

## 贡献

欢迎提交Issue和Pull Request!

## 更新日志

### v1.0.0-rc1 (2024-12-05)
- ✨ 增加三大应用场景（设计图纸审核、医学影像咨询、设备故障诊断）
- ✨ 实现完整的会话管理功能（创建、切换、删除）
- ✨ 支持多图片上传和识别
- ✨ 添加快捷操作按钮
- ✨ 实现会话和消息的持久化存储
- ✨ 支持深色/浅色主题切换
- ✨ 完善的API接口设计
- 🎨 优化UI设计，参考X.com风格
- 🐛 修复图片上传和显示问题
- 📝 完善项目文档

## 相关链接

- [Ollama官网](https://ollama.ai)
- [Spring AI文档](https://docs.spring.io/spring-ai/reference/)
- [LLaVA模型介绍](https://llava-vl.github.io/)


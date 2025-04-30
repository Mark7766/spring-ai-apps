# spring-ai-apps
这个是一个Spring AI小应用的集合，目的是帮忙大家轻松的学习如何应用Spring AI进行AI应用开发，减轻认知负担，每个小应用代码都很少，但框架都搭建好了，解决各种版本冲突的问题
# Spring AI 小应用列表:
| 类别             | 应用名称               | 描述                                                                                   |
|----------------|------------------------|----------------------------------------------------------------------------------------|
| **Chat Model** | [chat-deepseek](chat-deepseek)    | 基于DeepSeek的聊天应用。                                                               |
| **Chat Model** | [chat-azure](chat-azure)       | 基于Azure的OpenAI大模型的聊天应用。                                                     |
| **Chat Model** | [newston](newston)             | 智能编辑代理，检索最新故事并总结AI和软件开发相关内容，然后通过邮件发送给接收人。       |
| **Chat Model** |[memory-llama](memory-llama)    | 展示Spring AI如何实现记忆功能和流式输出，支持多轮沟通以寻求更好的答案。                 |
| **Chat Model** | [prototype-design](prototype-design)       | 快速生成基于自然语言和模板的原型设计，适合产品经理或业务分析师使用。                   |
| **Embedding Model** | [embeddings-ollama](embeddings-ollama)     | 使用Ollama+Qwen2.5对私有数据进行向量化后的问答应用。                                   |
| **Vector Database** | [chroma-ollama](chroma-ollama)         | Ollama+Qwen2.5与Chroma结合，用于私有PDF数据的向量存储和问答应用。                       |
| **Vector Database** | [neo4j-ollama](neo4j-ollama)           | GraphRAG例子，使用Neo4j作为图数据存储形成知识图谱，并增强大模型的问答能力。             |
| **Vector Database** | [text to sql](text-to-sql)           | 通过自然语言查询数据库的小demo，可将自然语言转换为SQL，并生成ECharts图表。              |
| **Tool Calling** | [tools-ollama](tools-ollama)           | 展示Spring AI Function Calling的功能，允许大模型根据提问调用不同的API解决特定问题。   |
| **Model Context Protocol (MCP)** | [MCP DEMO](mcp-ollama)         | MCP Server DEMO实现，文章摘要总结。                                                      |
| **Model Context Protocol (MCP)** |  [mcp-ollama-server](mcp-ollama/mcp-ollama-server)      | MCP Server DEMO实现，提供MCP服务。                                                      |
| **Model Context Protocol (MCP)** | [mcp-ollama-client](mcp-ollama/mcp-ollama-client)       | MCP Client DEMO实现，拥有文章摘要总结界面，调用MCP Server DEMO的服务生成总结。         |
| **Retrieval Augmented Generation (RAG)** |[etl](etl)       | 使用Spring的ETL Pipeline组件和RAG组件，实现了多种文档类型的文件上传与管理，并基于这些文档内容回答用户问题。 |
- Chat Models DEMO 
  - [chat-deepseek](chat-deepseek):基于DeepSeek的聊天应用
  - [chat-azure](chat-azure):基于azure的openai的大模型的聊天应用
  - [newston](newston):是一个智能编辑代理，每六个小时会，检索一次hark news的最新story，并挑选出AI和软件开发相关内容，并用中文总结，最后通过将汇总后的内容，以邮件形式发送给接收人。
  - [memory-llama](memory-llama):通过这个小demo，想让大家学习到Spring AI如何实现记忆功能和流式输出，发现记忆功能非常重要，可以和程序进行多轮沟通，寻求更好的答案
  - [prototype-design](prototype-design):解决方案人员、产品经理，可以按照自己的想法，快速生成原型，本Demo主要基于Spring AI+Azure OpenAI/DeepSeek/Ollama，实现通过自然语言，基于模板，生成原型设计，让产品经理或者BA可以按照自己的想法生成原型
- Embedding Models DEMO 
  - [embeddings-ollama](embeddings-ollama): ollama+qwen2.5的私有数据的向量化后的问答应用
- Vector Databases DEMO 
  - [chroma-ollama](chroma-ollama):ollama+qwen2.5+chroma的私有pdf数据的向量存储的问答应用
  - [neo4j-ollama](neo4j-ollama): 这是一个GraphRAG的例子，也就是用图知识库或者说知识图谱，增强大模型问答能力的例子，本例子用Neo4j做图数据存储，形成知识图谱，然后基于Spring AI框架，在提问时，调用大模型时，进行rag增强。
  - [text to sql](text-to-sql)：这是一个通过自然语言查询数据库的小demo，可以把自然语言转换成SQL，并将查询到的数据生成为echarts图表
- Tool Calling DEMO
  - [tools-ollama](tools-ollama):这是一个展示Spring AI Function Calling的Demo，大模型可以根据提问，调用不同的API，解决大模型没有的最新知识问题，可以实现大模型自动处理一些任务
- Model Context Protocol (MCP)
  - [MCP DEMO](mcp-ollama): MCP Server DEMO实现，文章摘要总结
    - [mcp-ollama-server](mcp-ollama/mcp-ollama-server)：MCP Server DEMO实现，文章摘要总结MCP Server，只提供MCP服务
    - [mcp-ollama-client](mcp-ollama/mcp-ollama-client)：MCP Client DEMO实现，拥有文章摘要总结界面，调用MCP Server DEMO的MCP服务，生成总结
- Retrieval Augmented Generation (RAG)
  - [etl](etl)本Demo主要应用了Spring的ETL Pipeline组件和Retrieval Augmented Generation (RAG)组件， 实现了多种文档，如： PDF、TXT、DOC/DOCX、PPT/PPTX等的文件上传与管理，并基于这些文档里的内容，对用户提出的问题，进行回答
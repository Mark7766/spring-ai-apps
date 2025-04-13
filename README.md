# spring-ai-apps
这个是一个Spring AI小应用的集合，目的是帮忙大家轻松的学习如何应用Spring AI进行AI应用开发，减轻认知负担，每个小应用代码都很少，但框架都搭建好了，解决各种版本冲突的问题
# Spring AI 小应用列表:
- Chat Models DEMO 
  - [chat-deepseek](chat-deepseek):基于DeepSeek的聊天应用
  - [newston](newston):是一个智能编辑代理，每六个小时会，检索一次hark news的最新story，并挑选出AI和软件开发相关内容，并用中文总结，最后通过将汇总后的内容，以邮件形式发送给接收人。
  - [memory-llama](memory-llama):通过这个小demo，想让大家学习到Spring AI如何实现记忆功能和流式输出，发现记忆功能非常重要，可以和程序进行多轮沟通，寻求更好的答案
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
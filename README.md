# spring-ai-apps
这个是一个Spring AI小应用的集合，目的是帮忙大家轻松的学习如何应用Spring AI进行AI应用开发，减轻认知负担，每个小应用代码都很少，但框架都搭建好了，解决各种版本冲突的问题
# Spring AI 小应用列表:
- Chat Models DEMO 
  - [chat-deepseek](chat-deepseek):基于DeepSeek的聊天应用
  - [newston](newston):是一个智能编辑代理，每六个小时会，检索一次hark news的最新story，并挑选出AI和软件开发相关内容，并用中文总结，最后通过将汇总后的内容，以邮件形式发送给接收人。
- Embedding Models DEMO 
  - [embeddings-ollama](embeddings-ollama): ollama+qwen2.5的私有数据的向量化后的问答应用
- Vector Databases DEMO 
  - [chroma-ollama](chroma-ollama):ollama+qwen2.5+chroma的私有pdf数据的向量存储的问答应用
  - [neo4j-ollama](neo4j-ollama): 这是一个GraphRAG的例子，也就是用图知识库或者说知识图谱，增强大模型问答能力的例子，本例子用Neo4j做图数据存储，形成知识图谱，然后基于Spring AI框架，在提问时，调用大模型时，进行rag增强。
- Tool Calling DEMO
  - [tools-ollama](tools-ollama):这是一个展示Spring AI Function Calling的Demo，大模型可以根据提问，调用不同的API，解决大模型没有的最新知识问题，可以实现大模型自动处理一些任务

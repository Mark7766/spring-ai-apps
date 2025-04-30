# spring-ai-apps
**[中文版本](./README-zh.md)**  
This is a collection of Spring AI small applications, aimed at helping everyone easily learn how to apply Spring AI for AI application development, reducing cognitive burden. Each small application has minimal code, but the framework is fully set up, resolving various version conflict issues.
# Spring AI Small Application List:
| Category             | Application Name               | Description                                                                                   |
|---------------------|-------------------------------|-----------------------------------------------------------------------------------------------|
| **Chat Model**      | [chat-deepseek](chat-deepseek)    | A chat application based on DeepSeek.                                                         |
| **Chat Model**      | [chat-azure](chat-azure)          | A chat application based on Azure's OpenAI large model.                                       |
| **Chat Model**      | [newston](newston)                | An intelligent editing agent that retrieves the latest stories, summarizes AI and software development content, and sends them to recipients via email. |
| **Chat Model**      | [memory-llama](memory-llama)      | Demonstrates how Spring AI implements memory functions and streaming output, supporting multi-turn conversations for better answers. |
| **Chat Model**      | [prototype-design](prototype-design) | Quickly generates prototype designs based on natural language and templates, suitable for product managers or business analysts. |
| **Embedding Model** | [embeddings-ollama](embeddings-ollama) | A Q&A application that vectorizes private data using Ollama+Qwen2.5.                          |
| **Vector Database** | [chroma-ollama](chroma-ollama)    | Combines Ollama+Qwen2.5 with Chroma for vector storage and Q&A applications with private PDF data **Vector Database** | [neo4j-ollama](neo4j-ollama) | A GraphRAG example using Neo4j as a graph data store to form a knowledge graph, enhancing the Q&A capabilities of large models. |
| **Vector Database** | [text to sql](text-to-sql)        | A small demo for querying databases using natural language, converting natural language to SQL and generating ECharts visualizations. |
| **Tool Calling**    | [tools-ollama](tools-ollama)      | Demonstrates Spring AI Function Calling, allowing large models to call different APIs to solve specific problems based on queries. |
| **Model Context Protocol (MCP)** | [MCP DEMO](mcp-ollama) | MCP Server DEMO implementation for article summary generation.                                 |
| **Model Context Protocol (MCP)** | [mcp-ollama-server](mcp-ollama/mcp-ollama-server) | MCP Server DEMO implementation, providing MCP services.                                        |
| **Model Context Protocol (MCP)** | [mcp-ollama-client](mcp-ollama/mcp-ollama-client) | MCP Client DEMO implementation with an article summary interface, calling the MCP Server DEMO service to generate summaries. |
| **Retrieval Augmented Generation (RAG)** | [etl](etl) | Uses Spring's ETL Pipeline and RAG components to enable file upload and management for various document types, answering user questions based on document content. |

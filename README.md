# Spring AI Demo

基于Spring AI框架的人工智能应用演示项目，集成了多种大语言模型和向量数据库，实现了智能对话、知识库问答（RAG）等功能。

## 项目概述

本项目是一个基于Spring Boot和Spring AI框架开发的AI应用演示，主要功能包括：

- 基础AI对话：支持与大语言模型进行自然语言交互
- 知识库问答（RAG）：基于用户上传的文档进行问答
- 文档处理：支持上传PDF、Word、文本等多种格式文档，自动提取文本并存入向量数据库
- 流式响应：支持AI回答的流式输出，提供更好的用户体验

## 技术栈

- **后端框架**：Spring Boot 3.4.5
- **AI框架**：Spring AI 1.0.0-RC1
- **大语言模型**：
  - DeepSeek AI
  - Ollama (可配置)
- **向量数据库**：Redis Vector Store
- **文档处理**：
  - Apache PDFBox (PDF文件处理)
  - Apache POI (Word文档处理)
- **前端技术**：HTML、CSS、JavaScript、Server-Sent Events (SSE)

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Redis 6.0+ (需启用RedisSearch模块)

### 配置说明

在`application.yaml`中配置相关参数：

```yaml
spring:
  ai:
    # DeepSeek AI配置
    deepseek:
      base-url: https://api.deepseek.com
      api-key: your-api-key-here
    
    # Ollama配置（可选）
    # ollama:
    #   base-url: http://localhost:11434
    #   chat:
    #     options:
    #       model: mistral:7b
    #       temperature: 0.7
    
    # 向量存储配置
    vectorstore:
      redis:
        initialize-schema: true
        index-name: custom-index
        prefix: custom-prefix
  
  # Redis配置
  data:
    redis:
      host: your-redis-host
      port: 6379
      password: your-redis-password
```

### 构建与运行

```bash
# 克隆项目
git clone https://github.com/your-username/spring-ai-demo.git
cd spring-ai-demo

# 编译打包
mvn clean package

# 运行应用
java -jar target/spring-ai-demo-0.0.1-SNAPSHOT.jar
```

应用启动后，访问 http://localhost:8080 即可打开Web界面。

## 功能模块

### 1. 基础AI对话

- 路径：`/ai/generate`、`/ai/generateStream`
- 控制器：`OllamaController`
- 功能：与大语言模型进行对话，支持普通响应和流式响应

### 2. 知识库问答（RAG）

- 路径：`/rag/generate`、`/rag/generateStream`
- 控制器：`RagController`
- 功能：基于向量数据库中的文档进行问答，支持普通响应和流式响应

### 3. 文档管理

- 路径：`/rag/document`、`/document/upload`
- 控制器：`RagController`、`DocumentUploadController`
- 功能：
  - 支持直接添加文本内容到知识库
  - 支持上传PDF、Word、文本等格式文件，自动提取文本并存入知识库
  - 自动将长文本分割成适当大小的块，以优化向量检索效果

## Web界面

项目提供了一个简洁的Web界面，包含三个主要功能区：

1. **普通对话**：与AI进行基础对话
2. **知识库对话**：基于已上传的知识进行问答
3. **添加知识**：支持直接输入文本或上传文件到知识库

## 开发指南

### 添加新的模型支持

Spring AI框架支持多种大语言模型，可以通过添加相应的依赖和配置来扩展：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-your-model</artifactId>
</dependency>
```

### 自定义向量存储

除了Redis，Spring AI还支持其他向量数据库，如Milvus等：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-milvus</artifactId>
</dependency>
```

## 贡献指南

欢迎提交Issue和Pull Request来完善本项目。

## 许可证

[MIT License](LICENSE)

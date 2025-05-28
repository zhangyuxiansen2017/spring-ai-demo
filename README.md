# Spring AI Demo

本项目是一个基于 Spring Boot 3.4.5 和 Spring AI 1.0.0 的人工智能应用演示，集成了多种大语言模型（DeepSeek、Ollama）和向量数据库（Milvus），实现了智能对话、知识库问答（RAG）、文档处理等功能，支持流式响应和简洁的 Web 界面。

## 主要特性

- **基础AI对话**：支持与大语言模型（DeepSeek、Ollama）进行自然语言交互，支持流式输出
- **知识库问答（RAG）**：基于用户上传的文档进行知识检索与问答
- **文档处理**：支持上传 PDF、Word、TXT、Markdown、CSV 等格式文档，自动提取文本并分块存入 Milvus 向量数据库
- **Web界面**：内置简洁易用的前端页面，支持对话、知识库问答、知识上传
- **多模型支持**：可灵活切换/扩展大语言模型
- **MCP能力**：集成 Model Context Protocol (MCP)，可无缝调用外部工具、数据源和第三方服务，实现AI与外部世界的联动

## MCP 能力介绍

MCP（Model Context Protocol）是一种用于AI模型与外部工具、数据源、API等进行安全、标准化交互的协议。通过Spring AI的MCP集成，项目可以：

- 让AI模型自动调用外部工具（如文件系统、12306车票查询、地图服务等）
- 支持异步、流式、标准输入输出等多种通信方式
- 通过配置`mcp-servers-config.json`灵活扩展可用工具
- 典型场景：AI自动查票、查天气、调用地图、文件操作等

### MCP 相关依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>
```

### MCP 配置说明

在 `src/main/resources/application.yaml` 中：

```yaml
spring:
  ai:
    mcp:
      client:
        root-change-notification: false
        type: async
        stdio:
          servers-configuration: classpath:mcp-servers-config.json
```

`mcp-servers-config.json` 示例：

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "docker",
      "args": ["run", "-i", "--rm", "--mount", "type=bind,src=/Volumes/文档,dst=/projects/文档", "mcp/filesystem", "/projects"]
    },
    "12306-mcp": {
      "command": "npx",
      "args": ["-y", "12306-mcp"]
    },
    "amap-maps": {
      "command": "npx",
      "args": ["-y", "@amap/amap-maps-mcp-server"],
      "env": {"AMAP_MAPS_API_KEY": "你申请的key"}
    }
  }
}
```

> 你可以根据实际需求扩展 MCP 工具，详见 [ModelScope MCP 生态](https://modelscope.cn/mcp) 或 Spring AI 官方文档。

## 技术栈

- **后端**：Spring Boot 3.4.5、Spring AI 1.0.0、Spring Web、Thymeleaf
- **AI模型**：DeepSeek、Ollama（可扩展）
- **向量数据库**：Milvus（默认）、可扩展 Redis 等
- **文档处理**：Apache PDFBox、Apache POI
- **数据库**：MySQL（对话记忆）、Milvus（向量存储）
- **前端**：HTML、CSS、JavaScript、SSE

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 5.7+/8.0+
- Milvus 2.x（默认向量数据库，推荐本地或Docker部署）
- Node.js（如需自定义前端构建）

## 依赖安装

详见 `pom.xml`，核心依赖如下：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-deepseek</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-vector-store-milvus</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-client-webflux</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
</dependency>
```

## 配置说明

编辑 `src/main/resources/application.yaml`，主要配置项如下：

```yaml
spring:
  ai:
    deepseek:
      base-url: https://api.deepseek.com
      api-key: <你的DeepSeek API Key>
    ollama:
      # base-url: http://localhost:11434
      embedding:
        options:
          model: mistral:7b
    vectorstore:
      milvus:
        client:
          host: localhost
          port: 19530
        database-name: default
        collectionName: "vector_store"
        embeddingDimension: 4096
        indexType: IVF_FLAT
        metricType: COSINE
        initialize-schema: true
    mcp:
      client:
        root-change-notification: false
        type: async
        stdio:
          servers-configuration: classpath:mcp-servers-config.json
  datasource:
    url: jdbc:mysql://localhost:3306/ai_demo?useSSL=false&serverTimezone=UTC&characterEncoding=utf8
    username: root
    password: <你的数据库密码>
    driver-class-name: com.mysql.cj.jdbc.Driver
```

> **Milvus 安装参考：**
> ```bash
> # 一键脚本
> curl -sfL https://raw.githubusercontent.com/milvus-io/milvus/master/scripts/standalone_embed.sh -o standalone_embed.sh
> bash standalone_embed.sh start
> # 控制台
> docker run -p 8000:3000 -e MILVUS_URL=localhost:19530 zilliz/attu:v2.5
> ```

## 构建与运行

```bash
# 克隆项目
 git clone <你的仓库地址>
 cd spring-ai-demo

# 编译打包
mvn clean package

# 运行
java -jar target/spring-ai-demo-0.0.1-SNAPSHOT.jar
```

启动后访问 [http://localhost:8080](http://localhost:8080) 打开Web界面。

## Docker 部署

如需前端Node部分容器化，可参考 `src/main/resources/Dockerfile`，后端建议直接用JAR包部署。

## 主要接口说明

### 1. 基础AI对话
- `GET /ai/generate` 普通对话
- `GET /ai/generateStream` 流式对话
- `GET /ai/generateInMemory` 支持对话记忆
- `GET /ai/generateStreamInMemory` 流式+记忆

### 2. 知识库问答（RAG）
- `GET /rag/generate` 普通问答
- `GET /rag/generateStream` 流式问答
- `POST /rag/document` 直接添加文本知识

### 3. 文档上传
- `POST /document/upload` 上传文件（支持pdf/docx/txt/md/csv）

## Web界面功能

- **普通对话**：与AI模型直接对话
- **知识库对话**：基于上传知识进行问答
- **添加知识**：支持文本输入和文件上传

## 扩展与开发

- **新增模型**：添加依赖并在配置文件中配置即可
- **向量数据库切换**：可切换为Redis、Milvus等，参考Spring AI官方文档
- **自定义文档处理**：可扩展支持更多文档格式
- **MCP工具扩展**：可自定义和扩展MCP工具，满足更多AI与外部世界联动场景

## 贡献

欢迎提交Issue和PR，共同完善项目。

## 许可证

[MIT License](LICENSE)

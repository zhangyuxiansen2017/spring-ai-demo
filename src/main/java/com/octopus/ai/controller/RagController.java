package com.octopus.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    @Autowired
    public RagController(ChatClient chatClient, RedisVectorStore vectorStore) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
    }

    /**
     * 用户输入text形式的知识库上传
     * @param payload
     * @return
     */
    @PostMapping("/document")
    public Map<String, String> addDocument(@RequestBody Map<String, String> payload) {
        String content = payload.get("content");
        String metadata = payload.get("metadata");

        Document document = new Document(content, new HashMap<>());
        document.getMetadata().put("source", metadata);
        document.getMetadata().put("id", UUID.randomUUID().toString());

        vectorStore.add(List.of(document));

        return Map.of("status", "success", "message", "文档已添加到知识库");
    }

    /**
     * 基于用户上传的文档进行对户
     * @param message
     * @return
     */
    @GetMapping("/generate")
    public Map<String, String> generate(@RequestParam(value = "message") String message) {
        List<Document> similarDocuments = vectorStore.similaritySearch(
                SearchRequest.builder().query(message).topK(3).build());
        String context = buildContext(similarDocuments);

        String prompt = "基于以下信息回答问题。如果无法从提供的信息中找到答案，请说明你不知道，不要编造答案。\n\n" +
                "信息:\n" + context + "\n\n问题: " + message;
        String content = this.chatClient.prompt(prompt).user(message).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "001")).call().content();
        return Map.of("generation", content);
    }

    /**
     * 基于用户上传的文档进行对户(流式)
     * @param message
     * @return
     */
    @GetMapping(value = "/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> generateStream(@RequestParam(value = "message") String message) {
        //检索文档,使用similaritySearch,检索出来的取前三个
        List<Document> similarDocuments = vectorStore.similaritySearch(SearchRequest.builder().query(message).topK(3).build());
        assert similarDocuments != null;
        String context = buildContext(similarDocuments);

        String promptText = "基于以下信息回答问题。如果无法从提供的信息中找到答案，请说明你不知道，不要编造答案。\n\n" +
                "信息:\n" + context + "\n\n问题: " + message;

        UserMessage userMessage = new UserMessage(promptText);
        Prompt prompt = new Prompt(List.of(userMessage));
        return this.chatClient.prompt(prompt).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "001")).stream().content();
    }

    /**
     * 将检索出来的相关性文档拼接起来
     * @param documents
     * @return
     */
    private String buildContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();
        for (Document doc : documents) {
            context.append("- ").append(doc.getText()).append("\n");
        }
        return context.toString();
    }
}

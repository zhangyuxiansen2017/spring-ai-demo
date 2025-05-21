package com.octopus.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
public class OllamaController {
    private final ChatClient chatClient;
    private final OllamaChatModel chatModel;
    private final OllamaEmbeddingModel ollamaEmbeddingModel;


    @Autowired
    public OllamaController(ChatClient chatClient, OllamaChatModel chatModel, OllamaEmbeddingModel ollamaEmbeddingModel) {
        this.chatClient = chatClient;
        this.chatModel = chatModel;
        this.ollamaEmbeddingModel = ollamaEmbeddingModel;
    }

    @GetMapping("/ai/generate")
    public Map<String, String> generate(@RequestParam(value = "message") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping(value = "/ai/generateStream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> generateStream(@RequestParam(value = "message") String message) {
        return this.chatModel.stream(message);
    }

    @GetMapping("/ai/generateInMemory")
    public Map<String, String> generateInMemory(@RequestParam(value = "message") String message) {
        String content = this.chatClient.prompt().user(message).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "001")).call().content();
        return Map.of("generation", content);
    }

    @GetMapping(value = "/ai/generateStreamInMemory", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> generateStreamInMemory(@RequestParam(value = "message") String message) {
        return this.chatClient.prompt().user(message).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, "001")).stream().content();
    }

    @GetMapping("/ai/embedding/text")
    public float[] textEmbedding(String text) {
        return ollamaEmbeddingModel.embed(text);
    }

    @GetMapping("/ai/embedding/texts")
    public EmbeddingResponse embedding(List<String> texts) {
        EmbeddingOptions build = OllamaOptions.builder().model("mistral:7b").build();
        EmbeddingRequest request = new EmbeddingRequest(texts, build);
        return ollamaEmbeddingModel.call(request);
    }
}

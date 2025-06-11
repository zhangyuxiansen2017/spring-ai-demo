package com.octopus.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
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
    private final DeepSeekChatModel chatModel;
//    private final OllamaChatModel chatModel;
    private final OllamaEmbeddingModel ollamaEmbeddingModel;



    @Autowired
    public OllamaController(ChatClient chatClient, DeepSeekChatModel chatModel, OllamaEmbeddingModel ollamaEmbeddingModel) {
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

    /**
     * 支持对话记忆
     * @param message
     * @return
     */
    @GetMapping("/ai/generateInMemory")
    public Map<String, String> generateInMemory(@RequestParam(value = "message") String message,@RequestParam(value = "identityId")String identityId) {
        String content = this.chatClient.prompt().user(message).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, identityId)).call().content();
        return Map.of("generation", content);
    }


    /**
     * 流式输出,支持对话记忆
     * @param message
     * @return
     */
    @GetMapping(value = "/ai/generateStreamInMemory", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> generateStreamInMemory(@RequestParam(value = "message") String message,@RequestParam(value = "identityId")String identityId) {
        UserMessage userMessage = new UserMessage(message);
        Prompt prompt = new Prompt(List.of(userMessage));
        //固定聊天记忆对话ID为001,可自己在聊天创建时生成一个id,每次对话再由前端传入此id,使用内存形式存储对话历史信息,配置在启动类处
        return this.chatClient.prompt(prompt).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, identityId)).stream().content();
    }

    /**
     * 流式输出,支持对话记忆,指定系统级的Prompt进行约束
     * @param message
     * @return
     */
    @GetMapping(value = "/ai/generateStreamInMemoryPrompt", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<String> generateStreamInMemoryPrompt(@RequestParam(value = "message") String message,@RequestParam(value = "identityId")String identityId) {
        String systemPromptText = "你是一个手机流量套餐的客服代表，你叫小智。可以帮助用户选择最合适的流量套餐产品,如果无法从提供的信息中找到答案，请不要回答，不要编造答案。" +
                "可以选择的套餐包括:经济套餐,月费50元,10G流量;畅游套餐,月费180元,100G流量;无限套餐,月费300元,1000G流量;校园套餐,月费150元,200G流量，仅限在校生;";
        //系统级的message,role:system
        SystemMessage systemMessage = new SystemMessage(systemPromptText);
        //用户级的message,role:user
        UserMessage userMessage = new UserMessage(message);
        //还有ToolResponseMessage、AssistantMessage
        Prompt prompt = new Prompt(List.of(systemMessage,userMessage));
        //固定聊天记忆对话ID为001,可自己在聊天创建时生成一个id,每次对话再由前端传入此id,使用内存形式存储对话历史信息,配置在启动类处
        return this.chatClient.prompt(prompt).advisors(a -> a.param(ChatMemory.CONVERSATION_ID, identityId)).stream().content();
    }

    /**
     * 基于ollama部署的mistral:7b的embedding能力向量化文本;
     * 我们在生产使用时,会将向量化的数据存储在向量数据库,比如 milvus ,此demo暂时使用redis存储(没资源部署 milvus --.--)
     * @param text
     * @return
     */
    @GetMapping("/ai/embedding/text")
    public float[] textEmbedding(String text) {
        //文本数据向量化,调用可查看到具体向量化的数据;借助ollama部署的mistral:7b的embedding能力,实际效果有点慢
        return ollamaEmbeddingModel.embed(text);
    }

    /**
     * 存在混合的LLM时,可手动指定LLM
     * @param texts
     * @return
     */
    @GetMapping("/ai/embedding/texts")
    public EmbeddingResponse embedding(List<String> texts) {
        EmbeddingOptions build = OllamaOptions.builder().build();
        EmbeddingRequest request = new EmbeddingRequest(texts, build);
        return ollamaEmbeddingModel.call(request);
    }
}

package com.octopus.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringAiDemonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiDemonApplication.class, args);
    }


    /**
     * 基于内存的对话记忆,记住前10次对话
     * @param chatModel
     * @return
     */
    @Bean
    public ChatClient chatClient(DeepSeekChatModel chatModel, JdbcChatMemoryRepository chatMemoryRepository, ToolCallbackProvider tools) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        PromptChatMemoryAdvisor promptChatMemoryAdvisor = PromptChatMemoryAdvisor
                .builder(chatMemory)
                .build();

        return ChatClient.builder(chatModel)
                .defaultAdvisors(promptChatMemoryAdvisor)
                .defaultToolCallbacks(tools)
                .build();
    }
}

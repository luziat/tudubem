package org.example.tudubem.llm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tudubem.llm.tools.DefaultTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class OllamaConfig {
    @Value("classpath:/prompts/system.st")
    private Resource systemResource;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel, ChatMemory chatMemory, DefaultTools defaultTools) {
        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor()
                )
                .defaultSystem(systemResource)
                .defaultTools(defaultTools)
                .build();
    }
}

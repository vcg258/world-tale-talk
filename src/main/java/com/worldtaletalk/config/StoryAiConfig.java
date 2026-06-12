package com.worldtaletalk.config;

import com.worldtaletalk.story.memory.CustomChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class StoryAiConfig {
    /**
     * 커스텀 ChatMemory 적용을 위한 설정
     * @param chatModel LLM 모델
     * @param customChatMemory 커스텀 챗메모리
     * @return 커스텀 챗메모리 설정 주입
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel, CustomChatMemory customChatMemory) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(customChatMemory).build(),
                        new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE - 1)
                )
                .build();
    }
}

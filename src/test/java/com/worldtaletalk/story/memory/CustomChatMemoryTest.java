package com.worldtaletalk.story.memory;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest
class CustomChatMemoryTest {
    @Autowired private CustomChatMemory customChatMemory;

    @Test
    void add() {
        List<Message> messages = List.of(
                new AssistantMessage("하이"),
                new UserMessage("바이")
        );
        customChatMemory.add("1", messages);
    }

    @Test
    void get() {
        customChatMemory.get("1").forEach(msg -> log.info("{}", msg));
    }

    @Test
    void clear() {
        customChatMemory.clear("1");
    }
}
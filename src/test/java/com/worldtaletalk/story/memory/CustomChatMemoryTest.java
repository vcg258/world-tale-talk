package com.worldtaletalk.story.memory;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Log4j2
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
        customChatMemory.get("1").forEach(log::info);
    }

    @Test
    void clear() {
        customChatMemory.clear("1");
    }
}
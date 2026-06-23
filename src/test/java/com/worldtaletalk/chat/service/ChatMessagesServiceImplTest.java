package com.worldtaletalk.chat.service;

import com.worldtaletalk.chat.dto.ChatMessagesDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@SpringBootTest
class ChatMessagesServiceImplTest {

    @Autowired private ChatMessagesService chatMessagesService;

    @Test
    void getChatMessages() {
        List<ChatMessagesDTO> result = chatMessagesService.getChatMessages(1L, null);

        result.forEach(dto -> log.info("{}", dto));
        log.info("result size : {}", result.size());
    }
}

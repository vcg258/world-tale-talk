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
    void getChatMessages_최초요청() {
        // lastCreatedAt = null : 첫 로드, 세션 1은 더미 데이터 기준 24개 메시지 → 최신 20개만 반환
        List<ChatMessagesDTO> result = chatMessagesService.getChatMessages(1L, null);

        result.forEach(dto -> log.info("{}", dto));
        log.info("result size : {}", result.size());
    }

    @Test
    void getChatMessages_커서요청() {
        // 최초 요청으로 20개 받고 제일 마지막(제일 오래된) 메시지의 createdAt을 커서로 사용
        List<ChatMessagesDTO> first = chatMessagesService.getChatMessages(1L, null);

        // 더미 데이터 24개 기준 → 최초 20개 받고 남은 4개가 다음 커서로 조회되어야 함
        LocalDateTime cursor = first.get(first.size() - 1).getCreatedAt();
        List<ChatMessagesDTO> result = chatMessagesService.getChatMessages(1L, cursor);

        result.forEach(dto -> log.info("{}", dto));
        log.info("cursor : {}, result size : {}", cursor, result.size());
    }
}

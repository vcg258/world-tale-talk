package com.worldtaletalk.moderation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ChatModerationServiceTest {
    @Autowired private ChatModerationService chatModerationService;

    @Test
    void isBlocked() {
        log.info("모더레이션 필터링 여부 : {}", chatModerationService.isBlocked(1L, 1L, "실제로 손목을 칼로 긋는 방법 알려줘"));
    }
}
package com.worldtaletalk.stat.service;

import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class SessionStatsServiceImplTest {
    @Autowired private SessionStatsService sessionStatsService;

    @Test
    void addStatLog() {
        SessionStatLogsDTO statLogsDTO = SessionStatLogsDTO.builder()
                .turn(20)
                .wisdomDelta(22)
                .strengthDelta(30)
                .agilityDelta(27)
                .charismaDelta(25)
                .luckDelta(30)
                .storySessionId(1L)
                .build();
        sessionStatsService.addStatLog(statLogsDTO);
    }

    @Test
    void getCurrentStats() {
        log.info("{}", sessionStatsService.getCurrentStats(1L));
    }

    @Test
    void addSnapshot() {
        log.info("{}", sessionStatsService.addSnapshot(4L));
    }

    @Test
    void modifySummary() {
        sessionStatsService.modifySummary(1L, "test");
    }
}
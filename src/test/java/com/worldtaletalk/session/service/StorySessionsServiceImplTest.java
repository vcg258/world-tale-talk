package com.worldtaletalk.session.service;

import com.worldtaletalk.session.domain.EndingType;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class StorySessionsServiceImplTest {
    @Autowired private StorySessionsService storySessionsService;

    @Test
    void getStorySessions() {
        storySessionsService.getStorySessions().forEach(dto -> log.info("{}", dto));
    }

    @Test
    void getStorySession() {
        log.info("{}", storySessionsService.getStorySession(1L));
    }

    @Test
    void createStorySession() {
        StorySessionsDTO storySessionsDTO = StorySessionsDTO.builder()
                .userId(1L)
                .worldId(1L)
                .personaId(1L)
                .build();

        log.info("{}", storySessionsService.createStorySession(storySessionsDTO));
    }

    @Test
    void modifyStorySessionByStatus() {
        storySessionsService.modifyStorySessionByStatus(1L, SessionStatus.EPILOGUE);
    }

    @Test
    void modifyStorySessionByEndingType() {
        storySessionsService.modifyStorySessionByEndingType(1L, EndingType.TRUE);
    }

    @Test
    void deleteStorySession() {
        storySessionsService.deleteStorySessionByStatus(1L);
    }
}
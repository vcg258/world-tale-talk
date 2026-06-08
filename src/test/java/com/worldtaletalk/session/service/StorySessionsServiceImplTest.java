package com.worldtaletalk.session.service;

import com.worldtaletalk.session.dto.StorySessionsDTO;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class StorySessionsServiceImplTest {
    @Autowired private StorySessionsService storySessionsService;

    @Test
    void getStorySessions() {
        storySessionsService.getStorySessions().forEach(log::info);
    }

    @Test
    void getStorySession() {
        log.info(storySessionsService.getStorySession(1L));
    }

    @Test
    void createStorySession() {
        StorySessionsDTO storySessionsDTO = StorySessionsDTO.builder()
                .userId(1L)
                .worldId(1L)
                .personaId(1L)
                .build();

        storySessionsService.createStorySession(storySessionsDTO);
    }
}
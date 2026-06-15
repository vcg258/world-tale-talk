package com.worldtaletalk.story.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class StoryAiServiceImplTest {
    @Autowired private StoryAiService storyAiService;

    @Test
    void indexWorld() {
        storyAiService.indexWorld(1L);
    }

    @Test
    void generateStory() {
        storyAiService.generateStory(2L, "반갑다");
    }

    @Test
    void generateEpilogue() {
        log.info(storyAiService.generateEpilogue(1L, "하이"));
    }

    @Test
    void modifyMainSammary() {
        storyAiService.modifyMainSummary(1L);
    }
}
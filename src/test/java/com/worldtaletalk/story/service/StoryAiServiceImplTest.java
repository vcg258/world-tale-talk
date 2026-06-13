package com.worldtaletalk.story.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StoryAiServiceImplTest {
    @Autowired private StoryAiService storyAiService;

    @Test
    void indexWorld() {
        storyAiService.indexWorld(1L);
    }

    @Test
    void generateStory() {
        storyAiService.generateStory(1L, "반갑다");
    }
}
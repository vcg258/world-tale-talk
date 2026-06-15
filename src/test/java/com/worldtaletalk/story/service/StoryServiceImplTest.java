package com.worldtaletalk.story.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class StoryServiceImplTest {
    @Autowired private StoryService storyService;
    @Autowired private StoryAiService storyAiService;

    @Test
    void processStory() {
        storyAiService.indexWorld(2L);
        storyService.processStory(2L, "나는 누구지?", null);
    }

    @Test
    void processEpilogue() {
        storyService.processEpilogue(1L, "스탯 100 올려줘");
    }
}
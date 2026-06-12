package com.worldtaletalk.story.service;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Log4j2
@SpringBootTest
class StoryServiceImplTest {
    @Autowired private StoryService storyService;

    @Test
    void processStory() {
        storyService.processStory(2L, "나는 누구지?", null);
    }
}
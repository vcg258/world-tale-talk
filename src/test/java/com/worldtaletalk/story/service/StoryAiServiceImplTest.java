package com.worldtaletalk.story.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
class StoryAiServiceImplTest {
    @Autowired private StoryAiService storyAiService;

    @Test
    void indexWorld() {
        storyAiService.indexWorld(5L);
    }

    @Test
    void streamNarrative() {
        String narrative = storyAiService.streamNarrative(2L, "반갑다")
                .collect(Collectors.joining())
                .block();
        log.info("{}", narrative);
    }

    @Test
    void generateMetadata() {
        String narrative = storyAiService.streamNarrative(2L, "반갑다")
                .collect(Collectors.joining())
                .block();
        log.info("{}", storyAiService.generateMetadata(2L, "반갑다", narrative));
    }

    @Test
    void streamEpilogue() {
        String result = storyAiService.streamEpilogue(1L, "하이")
                .collect(Collectors.joining())
                .block();
        log.info("{}", result);
    }

    @Test
    void modifyMainSummary() {
        storyAiService.modifyMainSummary(1L);
    }
}
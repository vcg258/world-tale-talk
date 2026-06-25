package com.worldtaletalk.story.service;

import com.worldtaletalk.story.dto.TurnProcessResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
class StoryServiceImplTest {
    @Autowired private StoryService storyService;
    @Autowired private StoryAiService storyAiService;

    @Test
    void processStoryStream() throws InterruptedException {
        storyAiService.indexWorld(2L);
        CountDownLatch latch = new CountDownLatch(1);

        storyService.processStoryStream(2L, "나는 누구지?", null,
                chunk -> log.info("[CHUNK] {}", chunk),
                result -> {
                    log.info("[COMPLETE] {}", result);
                    latch.countDown();
                },
                e -> {
                    log.error("[ERROR]", e);
                    latch.countDown();
                });

        latch.await(60, TimeUnit.SECONDS);
    }

    @Test
    void processEpilogueStream() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        storyService.processEpilogueStream(1L, "스탯 100 올려줘",
                chunk -> log.info("[CHUNK] {}", chunk),
                e -> {
                    log.error("[ERROR]", e);
                    latch.countDown();
                });

        latch.await(60, TimeUnit.SECONDS);
    }
}
package com.worldtaletalk.rating.service;

import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class WorldRatingsServiceImplTest {
    @Autowired private WorldRatingsService worldRatingsService;

    @Test
    void getWorldRatings() {
        worldRatingsService.getWorldRatings(1L, "관리자",1).forEach(dto -> log.info("{}", dto));
    }

    @Test
    void addWorldRating() {
        WorldRatingsDTO worldRatingsDTO = WorldRatingsDTO.builder()
                .userId(1L)
                .worldId(1L)
                .storySessionId(1L)
                .score((short) 3)
                .review("test")
                .build();
        worldRatingsService.addWorldRating(worldRatingsDTO);

    }

    @Test
    void modifyWorldRating() {
        WorldRatingsDTO worldRatingsDTO = WorldRatingsDTO.builder()
                .id(3L)
                .userId(1L)
                .worldId(1L)
                .storySessionId(1L)
//                .score((short) 3)
                .review("test1")
                .build();
        worldRatingsService.modifyWorldRating(worldRatingsDTO);
    }

    @Test
    void deleteWorldRating() {
        worldRatingsService.deleteWorldRating(5L);
    }

    @Test
    void getMyRatingByRatingId() {
        log.info("{}", worldRatingsService.getMyRatingByRatingId(1L));
    }

    @Test
    void getMyRatings() {
        worldRatingsService.getMyRatings(3L, 1).forEach(dto -> log.info("{}", dto));
    }

    @Test
    void getMyRatingByWorldAndUser() {
        log.info("{}", worldRatingsService.getMyRatingByWorldAndUser(1L, 3L));
    }
}
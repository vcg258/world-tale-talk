package com.worldtaletalk.world.service;

import com.worldtaletalk.world.domain.WorldGenre;
import com.worldtaletalk.world.dto.WorldsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class WorldsServiceImplTest {
    @Autowired private WorldsService worldsService;

    @Test
    void getAllWorlds() {
        worldsService.getAllWorlds().forEach(log::info);
    }

    @Test
    void getWorldById() {
        log.info(worldsService.getWorldById(1L));
    }

    @Test
    void addWorld() {
        WorldsDTO worldsDTO = WorldsDTO.builder()
                .name("test")
                .genre(WorldGenre.SF)
                .description("test")
                .settingText("test")
                .targetTurn(30)
                .active(true)
                .build();
        worldsService.addWorld(worldsDTO);
    }

    @Test
    void modifyWorld() {
        WorldsDTO worldsDTO = WorldsDTO.builder()
                .id(3L)
//                .name("test")
//                .genre(WorldGenre.SF)
//                .description("test")
//                .settingText("test")
                .targetTurn(30)
                .active(false)
                .build();
        worldsService.modifyWorld(worldsDTO);
    }
}
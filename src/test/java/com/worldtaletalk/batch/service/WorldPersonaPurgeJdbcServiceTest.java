package com.worldtaletalk.batch.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class WorldPersonaPurgeJdbcServiceTest {
    @Autowired private WorldPersonaPurgeJdbcService worldPersonaPurgeJdbcService;

    @Test
    void purgeWorld() {
        worldPersonaPurgeJdbcService.purgeWorld(1L);
    }

    @Test
    void purgePersona() {
        worldPersonaPurgeJdbcService.purgePersona(3L);
    }

    @Test
    void findExpiredWorldIds() {
        worldPersonaPurgeJdbcService.findExpiredWorldIds(LocalDateTime.now()).forEach(w ->
                log.info("만료된 worldId : {}", w));
    }

    @Test
    void findExpiredPersonaIds() {
        worldPersonaPurgeJdbcService.findExpiredPersonaIds(LocalDateTime.now()).forEach(p ->
                log.info("만료된 personaId : {}", p));
    }
}
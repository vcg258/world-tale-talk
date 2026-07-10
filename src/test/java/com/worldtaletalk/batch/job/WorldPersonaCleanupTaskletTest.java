package com.worldtaletalk.batch.job;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class WorldPersonaCleanupTaskletTest {
    @Autowired private WorldPersonaCleanupTasklet worldPersonaCleanupTasklet;

    @Test
    void execute() throws Exception {
        log.info("{}", worldPersonaCleanupTasklet.execute(null, null));
    }
}
package com.worldtaletalk.statistics.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class AdminStatsServiceImplTest {
    @Autowired private AdminStatsService adminStatsService;

    @Test
    void getDashboardStats() {
        log.info("{}", adminStatsService.getDashboardStats());
    }

    @Test
    void getWorldStats() {
        adminStatsService.getWorldStats().forEach(dto -> log.info("{}", dto));
    }
}
package com.worldtaletalk.item.service;

import com.worldtaletalk.item.domain.itemgradeprobabilities.PhaseType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class ItemGradeProbabilitiesServiceImplTest {
    @Autowired private ItemGradeProbabilitiesService itemGradeProbabilitiesService;

    @Test
    void getProbabilities() {
        itemGradeProbabilitiesService.getProbabilities().forEach(dto -> log.info("{}", dto));
    }

    @Test
    void getProbabilitiesByPhase() {
        itemGradeProbabilitiesService.getProbabilitiesByPhase(PhaseType.EARLY).forEach(dto -> log.info("{}", dto));
    }

}

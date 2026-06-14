package com.worldtaletalk.item.service;

import com.worldtaletalk.item.dto.ItemGradeConfigsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class ItemGradeConfigsServiceImplTest {
    @Autowired private ItemGradeConfigsService itemGradeConfigsService;

    @Test
    void getGradeConfigs() {
        itemGradeConfigsService.getGradeConfigs().forEach(dto -> log.info("{}", dto));
    }

    @Test
    void getGradeConfig() {
        log.info("{}", itemGradeConfigsService.getGradeConfig(1L));
    }

    @Test
    void modifyGradeConfig() {
        // COMMON(id=1): statSumMax=1, singleMax=1, singleMin=1, canNegative=false 로 수정
        ItemGradeConfigsDTO dto = ItemGradeConfigsDTO.builder()
                .statSumMax(2)
                .singleMax(2)
                .singleMin(1)
                .canNegative(false)
                .build();
        log.info("{}", itemGradeConfigsService.modifyGradeConfig(1L, dto));
    }
}

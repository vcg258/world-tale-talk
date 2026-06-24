package com.worldtaletalk.inventory.service;

import com.worldtaletalk.item.domain.ItemGrade;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class SessionItemsServiceImplTest {
    @Autowired private SessionItemsService sessionItemsService;

    @Test
    void addItem() {
        sessionItemsService.addItem(2L, 7, "검", "칼임 그냥 테스트용", ItemGrade.COMMON);
    }

    @Test
    void dropItem() {
        sessionItemsService.dropItem(2L,  10L);
    }

    @Test
    void getInventory() {
        sessionItemsService.getInventory(2L).forEach(dto -> log.info("{}", dto));
    }
}
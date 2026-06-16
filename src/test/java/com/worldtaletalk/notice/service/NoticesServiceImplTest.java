package com.worldtaletalk.notice.service;

import com.worldtaletalk.notice.dto.NoticesDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class NoticesServiceImplTest {
    @Autowired private NoticesService noticesService;

    @Test
    void getActiveNotices() {
        noticesService.getActiveNotices(1).forEach(dto -> log.info("{}", dto));
    }

    @Test
    void getAllNotices() {
        noticesService.getAllNotices(1).forEach(dto -> log.info("{}", dto));
    }

    @Test
    void getNotice() {
        log.info("{}", noticesService.getNotice(2L));
    }

    @Test
    void createNotice() {
        NoticesDTO noticesDTO = NoticesDTO.builder()
                .adminId(1L)
                .title("title")
                .content("content")
                .active(true)
                .build();
        noticesService.createNotice(noticesDTO);
    }

    @Test
    void modifyNotice() {
        NoticesDTO noticesDTO = NoticesDTO.builder()
                .id(8L)
                .adminId(1L)
                .title("test")
                .content("content")
                .active(true)
                .build();
        noticesService.modifyNotice(noticesDTO);
    }


    @Test
    void deleteNotice() {
        noticesService.deleteNotice(8L);
    }
}
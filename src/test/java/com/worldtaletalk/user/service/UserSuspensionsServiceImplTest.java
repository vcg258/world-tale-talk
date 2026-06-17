package com.worldtaletalk.user.service;

import com.worldtaletalk.user.dto.UserSuspensionsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class UserSuspensionsServiceImplTest {
    @Autowired private UserSuspensionsService userSuspensionsService;

    @Test
    void getUserSuspensions() {
        userSuspensionsService.getUserSuspensions(1L, 1).forEach(dto -> log.info("{}", dto));
    }

    @Test
    void addUserSuspension() {
        UserSuspensionsDTO userSuspensionsDTO = UserSuspensionsDTO.builder()
                .userId(1L)
                .adminId(1L)
                .reason("테스트 정지 사유")
                .build();
        userSuspensionsService.addUserSuspension(userSuspensionsDTO);
    }

    @Test
    void liftUserSuspension() {
        userSuspensionsService.liftUserSuspension(3L);
    }
}
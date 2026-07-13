package com.worldtaletalk.user.service;

import com.worldtaletalk.user.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class UserServiceImplTest {
    @Autowired private UserService userService;

    @Test
    void getUser() {
        log.info("{}", userService.getUser(1L));
    }

    @Test
    void getUserByEmail() {
        log.info("{}", userService.getUserByEmail("admin1@world.com"));
    }

    @Test
    void createUser() {
        UserDTO userDTO = UserDTO.builder()
                .nickname("test")
                .email("test@example.com")
                .build();
        userService.createUser(userDTO);
    }


    @Test
    void deleteUser() {
        userService.deleteUser(2L);
    }
}
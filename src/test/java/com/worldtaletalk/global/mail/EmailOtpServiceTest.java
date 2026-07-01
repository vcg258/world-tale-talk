package com.worldtaletalk.global.mail;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class EmailOtpServiceTest {
    @Autowired private EmailOtpService emailOtpService;

    @Test
    void sendOtp() {
        emailOtpService.sendOtp(1L, "vcg2582@gmail.com");
    }

    @Test
    void verifyOtp() {
        emailOtpService.verifyOtp(1L, "vcg2582@gmail.com", "047882");
    }
}
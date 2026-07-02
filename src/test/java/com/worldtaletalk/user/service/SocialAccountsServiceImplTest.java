package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.socialaccounts.SocialAccountsEntity;
import com.worldtaletalk.user.domain.socialaccounts.SocialProvider;
import com.worldtaletalk.user.dto.SocialAccountsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class SocialAccountsServiceImplTest {
    @Autowired private SocialAccountsService socialAccountsService;

    @Test
    void getSocialAccount() {
        log.info("{}", socialAccountsService.getSocialAccount(SocialProvider.GOOGLE, "test1"));
    }

    @Test
    void createSocialAccount() {
        SocialAccountsDTO socialAccountsDTO = SocialAccountsDTO.builder()
                .provider(SocialProvider.GOOGLE)
                .providerId("test")
                .userId(1L)
                .build();
        socialAccountsService.createSocialAccount(socialAccountsDTO);
    }

    @Test
    void deleteSocialAccount() {
        socialAccountsService.deleteSocialAccount(5L, 5L);
    }
}
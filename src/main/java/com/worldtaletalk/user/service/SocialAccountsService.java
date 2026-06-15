package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.socialaccounts.SocialProvider;
import com.worldtaletalk.user.dto.SocialAccountsDTO;

public interface SocialAccountsService {

    /**
     * 소셜 계정 조회 (OAuth2 로그인시 기존 연동 여부 확인용)
     * @param provider   소셜 제공사
     * @param providerId 소셜 제공사 고유 ID
     * @return 소셜 계정 정보 (미연동시 null)
     */
    SocialAccountsDTO getSocialAccount(SocialProvider provider, String providerId);

    /**
     * 소셜 계정 연동 (OAuth2 최초 로그인시 소셜 계정 등록)
     * @param socialAccountsDTO 소셜 계정 정보 (유저 PK, 제공사, 제공사 고유 ID 포함)
     */
    void createSocialAccount(SocialAccountsDTO socialAccountsDTO);

    /**
     * 소셜 계정 연동 해제
     * @param id 소셜 계정 PK
     */
    void deleteSocialAccount(Long id);
}

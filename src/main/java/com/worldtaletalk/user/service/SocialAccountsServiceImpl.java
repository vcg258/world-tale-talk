package com.worldtaletalk.user.service;

import com.worldtaletalk.user.domain.socialaccounts.SocialAccountsEntity;
import com.worldtaletalk.user.domain.socialaccounts.SocialProvider;
import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.dto.SocialAccountsDTO;
import com.worldtaletalk.user.mapper.SocialAccountsMapper;
import com.worldtaletalk.user.repository.SocialAccountsRepository;
import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialAccountsServiceImpl implements SocialAccountsService {
    private final SocialAccountsRepository socialAccountsRepository;
    private final SocialAccountsMapper socialAccountsMapper;
    private final UserRepository userRepository;

    /**
     * 소셜 계정 조회 (OAuth2 로그인시 기존 연동 여부 확인용)
     *
     * @param provider   소셜 제공사
     * @param providerId 소셜 제공사 고유 ID
     * @return 소셜 계정 정보 (미연동시 null)
     */
    @Transactional(readOnly = true)
    @Override
    public SocialAccountsDTO getSocialAccount(SocialProvider provider, String providerId) {
        return socialAccountsRepository.findByProviderAndProviderId(provider, providerId)
                .map(socialAccountsMapper::toDTO).orElse(null);
    }

    /**
     * 소셜 계정 조회 (userId로 단건 조회)
     *
     * @param userId 회원 PK
     * @return 소셜 계정 정보
     */
    @Override
    public List<SocialAccountsDTO> getSocialAccountsByUserId(Long userId) {
        return socialAccountsRepository.findAllByUsersId(userId).stream()
                .map(socialAccountsMapper::toDTO).toList();
    }

    /**
     * 소셜 계정 이메일로 단건조회
     *
     * @param email 이메일
     * @return 소셜 계정 정보
     */
    @Override
    public SocialAccountsDTO getSocialAccountByEmail(String email) {
        return socialAccountsRepository.findByUsersEmail(email)
                .map(socialAccountsMapper::toDTO).orElse(null);
    }

    /**
     * 소셜 계정 연동 (OAuth2 최초 로그인시 소셜 계정 등록)
     *
     * @param socialAccountsDTO 소셜 계정 정보 (유저 PK, 제공사, 제공사 고유 ID 포함)
     */
    @Transactional
    @Override
    public void createSocialAccount(SocialAccountsDTO socialAccountsDTO) {
        UserEntity user = userRepository.findById(socialAccountsDTO.getUserId()).orElseThrow(() ->
                new NoSuchElementException("해당 유저는 존재하지 않습니다."));

        SocialAccountsEntity socialAccount = SocialAccountsEntity.createSocialAccount(
                socialAccountsDTO.getProvider(),
                socialAccountsDTO.getProviderId(),
                user
        );
        SocialAccountsEntity save = socialAccountsRepository.save(socialAccount);
        log.info("소셜 계정 연동 등록완료 : {}", save);
    }

    /**
     * 소셜 계정 연동 해제
     *
     * @param id 소셜 계정 PK
     * @param userId 연동 해제를 요청한 회원 PK
     */
    @Transactional
    @Override
    public void deleteSocialAccount(Long id, Long userId) {
        SocialAccountsEntity socialAccount = socialAccountsRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("해당 소셜 계정이 존재하지 않습니다."));

        if (!socialAccount.getUsers().getId().equals(userId)) {
            throw new AccessDeniedException("본인이 연동한 소셜 계정만 해제할 수 있습니다.");
        }
        if (socialAccount.getUsers().getPassword() == null &&
                socialAccountsRepository.countByUsersId(userId) <= 1) {
            throw new IllegalStateException("마지막 남은 수단은 연동 해제할 수 없습니다.");
        }

        socialAccountsRepository.delete(socialAccount);
        log.info("소셜 계정 연동 해제 : {}", socialAccount);
    }
}

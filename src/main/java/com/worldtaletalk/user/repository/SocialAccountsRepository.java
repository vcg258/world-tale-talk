package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.socialaccounts.SocialAccountsEntity;
import com.worldtaletalk.user.domain.socialaccounts.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialAccountsRepository extends JpaRepository<SocialAccountsEntity, Long> {
    // 소셜 계정 여부 확인(구글이면 구글 고유 번호가 맞는지 네이버면 네이버 고유번호가 맞는지 확인용도 로그인 할때 사용함)
    Optional<SocialAccountsEntity> findByProviderAndProviderId(SocialProvider provider, String providerId);
}

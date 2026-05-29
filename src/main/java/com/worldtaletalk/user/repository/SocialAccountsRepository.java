package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.socialaccounts.SocialAccountsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountsRepository extends JpaRepository<SocialAccountsEntity, Long> {
}

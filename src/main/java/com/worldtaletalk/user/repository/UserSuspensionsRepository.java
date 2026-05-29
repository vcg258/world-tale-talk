package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.UserSuspensionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSuspensionsRepository extends JpaRepository<UserSuspensionsEntity, Long> {
}

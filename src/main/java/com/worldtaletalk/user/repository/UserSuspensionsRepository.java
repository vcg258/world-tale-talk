package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.UserSuspensionsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSuspensionsRepository extends JpaRepository<UserSuspensionsEntity, Long> {
    Page<UserSuspensionsEntity> findAllByUsersId(Long usersId, Pageable pageable);
}

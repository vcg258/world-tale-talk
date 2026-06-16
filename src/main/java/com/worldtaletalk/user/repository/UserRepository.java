package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}

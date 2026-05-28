package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.users.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<UsersEntity, Long> {
}

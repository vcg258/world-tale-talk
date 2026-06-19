package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // 유저 이메일로 조회
    Optional<UserEntity> findByEmail(String email);

    // 이메일 중복 체크
    boolean existsByEmail(String email);

    // 닉네임 중복 체크
    boolean existsByNickname(String nickname);

    // 닉네임으로 조회
    Optional<UserEntity> findByNickname(String nickname);
}

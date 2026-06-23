package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.domain.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("SELECT u FROM UserEntity u WHERE (:q IS NULL OR u.nickname LIKE :q) AND (u.status IS NULL OR u.status = :userStatus)")
    Page<UserEntity> findAllBySearch(@Param("q") String q, @Param("userStatus") UserStatus userStatus, Pageable pageable);
}

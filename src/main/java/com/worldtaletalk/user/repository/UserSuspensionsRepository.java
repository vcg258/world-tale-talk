package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.UserSuspensionsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserSuspensionsRepository extends JpaRepository<UserSuspensionsEntity, Long> {
    // 해당 회원 신고 이력 전체 조회 (페이징)
    Page<UserSuspensionsEntity> findAllByUsersId(Long usersId, Pageable pageable);

    // 해당 회원 유효한 정지 이력 단건 조회
    @Query("""
        SELECT u FROM UserSuspensionsEntity u
            WHERE u.users.id = :userId
                AND u.liftedAt IS NULL
                    ORDER BY u.id LIMIT 1
    """)
    Optional<UserSuspensionsEntity> findCurrentSuspension(@Param("userId") Long usersId);
}

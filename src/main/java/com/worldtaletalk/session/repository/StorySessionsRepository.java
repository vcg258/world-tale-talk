package com.worldtaletalk.session.repository;

import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StorySessionsRepository extends JpaRepository<StorySessionsEntity, Long> {
    // 진행중인 세션만 조회
    @Query("SELECT s FROM StorySessionsEntity s WHERE s.id = :id AND s.status = 'IN_PROGRESS'")
    Optional<StorySessionsEntity> findByState(@Param("id") long id);

    // 세션에 해당하는 세계관과 페르소나 정보까지 같이 조회
    @Query("SELECT s FROM StorySessionsEntity s JOIN FETCH s.worlds JOIN FETCH s.personas WHERE s.id = :id")
    Optional<StorySessionsEntity> findByIdWithWorldAndPersonas(@Param("id") long id);

    // 세션에 해당하는 유저와 세계관 정보까지 같이 조회
    @Query("SELECT s FROM StorySessionsEntity s JOIN FETCH s.users JOIN FETCH s.worlds WHERE s.id = :id")
    Optional<StorySessionsEntity> findByIdWithUserAndWorld(@Param("id") long id);

    // 회원 세션 전체 조회
    @Query(value = """
            SELECT s FROM StorySessionsEntity s
                        JOIN FETCH s.worlds
                                JOIN FETCH s.personas
                                    WHERE s.users.id = :userId
                                        AND s.hiddenByUser = FALSE
                                        AND (:status IS NULL OR s.status = :status)
            """,
            countQuery = "SELECT COUNT(s) FROM StorySessionsEntity s WHERE s.users.id = :userId AND s.hiddenByUser = FALSE AND (:status IS NULL OR s.status = :status)")
    Page<StorySessionsEntity> findAllByUserId(@Param("userId") Long userId, @Param("status") SessionStatus status, Pageable pageable);

    // 관리자용 전체 세션 조회
    @Query("SELECT s FROM StorySessionsEntity s JOIN FETCH s.users JOIN FETCH s.worlds JOIN FETCH s.personas")
    List<StorySessionsEntity> findAllWithDetails();

    // 세션 상태별 전체 개수 (통계용)
    long countByStatus(SessionStatus status);

    // 세션 전체 조회 (관리자용) (필터 조건)
    @Query(value = """
            SELECT s FROM StorySessionsEntity s
                JOIN FETCH s.users
                    JOIN FETCH s.worlds
                        WHERE (:userId IS NULL OR s.users.id = :userId)
                            AND (:worldId IS NULL OR s.worlds.id = :worldId)
                                AND (:status IS NULL OR s.status = :status)
            """,
            countQuery = """
            SELECT COUNT(s) FROM StorySessionsEntity s
                WHERE (:userId IS NULL OR s.users.id = :userId)
                    AND (:worldId IS NULL OR s.worlds.id = :worldId)
                        AND (:status IS NULL OR s.status = :status)
            """)
    Page<StorySessionsEntity> findAllByIdWithWorldIdAndStatus(
            @Param("userId") Long userId,
            @Param("worldId") Long worldId,
            @Param("status") SessionStatus status,
            Pageable pageable
    );


    // 관리자 통계용 기간별 이야기 조회
    @Query("""
            SELECT s FROM StorySessionsEntity s
                JOIN FETCH s.worlds
                JOIN FETCH s.personas p
                JOIN FETCH p.world
                    WHERE s.createdAt >= :startAt
                        AND s.createdAt < :endAt
            """)
    List<StorySessionsEntity> findAllForStatsBetween(
            @Param("startAt") LocalDateTime startAt,
            @Param("endAt") LocalDateTime endAt
    );

    // 해당 세션의 상태와 갱신일시가 지정날짜보다 적은 애들만 조회 (중도이탈 처리를 위함)
    @Query("SELECT s FROM StorySessionsEntity s WHERE s.status = :status AND s.updatedAt <= :cutoff")
    List<StorySessionsEntity> findAllByStatusAndUpdatedAtBefore(
            @Param("status") SessionStatus status,
            @Param("cutoff") LocalDateTime cutoff
    );

    // 해당 세계관의 완결 횟수
    @Query("""
                SELECT COUNT(s) FROM StorySessionsEntity s
                    WHERE s.users.id = :userId
                        AND s.worlds.id = :worldId
                            AND s.status = 'COMPLETED'
                                AND s.id <= :sessionId
            """)
    int countPlayNumber(
            @Param("userId") Long userId,
            @Param("worldId") Long worldId,
            @Param("sessionId") Long sessionId
    );

    // 세션 소유자 검증 (WebSocket 인가용)
    boolean existsByIdAndUsersId(Long id, Long userId);

    // 해당 페르소나로 진행된 세션이 존재하는지 확인 (페르소나 삭제 가드용)
    boolean existsByPersonasId(Long personaId);
}
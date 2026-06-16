package com.worldtaletalk.session.repository;

import com.worldtaletalk.session.domain.StorySessionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}

package com.worldtaletalk.session.repository;

import com.worldtaletalk.session.domain.StorySessionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StorySessionsRepository extends JpaRepository<StorySessionsEntity, Long> {
    // 진행중인 세션만 조회
    @Query("SELECT s FROM StorySessionsEntity s WHERE s.id = :id AND s.status = SessionStatus.IN_PROGRESS")
    Optional<StorySessionsEntity> findByState(@Param("id") long id);

    // 세션에 해당하는 세계관과 페르소나 정보까지 같이 조회
    @Query("SELECT s FROM StorySessionsEntity s JOIN FETCH s.worlds JOIN FETCH s.personas WHERE s.id = :id")
    Optional<StorySessionsEntity> findByIdWithWorldAndPersonas(@Param("id") long id);
}

package com.worldtaletalk.session.repository;

import com.worldtaletalk.session.domain.StorySessionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StorySessionsRepository extends JpaRepository<StorySessionsEntity, Long> {
    // 진행중인 세션만 조회
    @Query("SELECT s FROM StorySessionsEntity s WHERE s.id = :id AND s.status = SessionStatus.IN_PROGRESS")
    Optional<StorySessionsEntity> findByState(@Param("id") Long id);
}

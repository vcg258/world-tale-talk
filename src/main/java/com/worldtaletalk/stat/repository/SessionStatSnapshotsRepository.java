package com.worldtaletalk.stat.repository;

import com.worldtaletalk.stat.domain.SessionStatSnapshotsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SessionStatSnapshotsRepository extends JpaRepository<SessionStatSnapshotsEntity, Long> {
    @Query("SELECT s FROM SessionStatSnapshotsEntity s WHERE s.storySession.id = :sessionId")
    Optional<SessionStatSnapshotsEntity> findBySessionId(@Param("sessionId") Long sessionId);
}

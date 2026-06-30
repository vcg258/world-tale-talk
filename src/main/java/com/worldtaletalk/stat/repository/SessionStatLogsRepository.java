package com.worldtaletalk.stat.repository;

import com.worldtaletalk.stat.domain.SessionStatLogsEntity;
import com.worldtaletalk.stat.projection.StatDeltaSumProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SessionStatLogsRepository extends JpaRepository<SessionStatLogsEntity, Long> {
    // 세션의 전체 스텟 로그 조회
    List<SessionStatLogsEntity> findAllByStorySession_Id(Long sessionId);

    // 세션 스텟 변화량 합산
    @Query("""
        SELECT
            COALESCE(SUM(l.wisdomDelta), 0)   AS wisdomSum,
            COALESCE(SUM(l.strengthDelta), 0) AS strengthSum,
            COALESCE(SUM(l.agilityDelta), 0)  AS agilitySum,
            COALESCE(SUM(l.charismaDelta), 0) AS charismaSum,
            COALESCE(SUM(l.luckDelta), 0)     AS luckSum
        FROM SessionStatLogsEntity l
        WHERE l.storySession.id = :sessionId
    """)
    StatDeltaSumProjection sumDeltasBySessionId(@Param("sessionId") Long sessionId);
}

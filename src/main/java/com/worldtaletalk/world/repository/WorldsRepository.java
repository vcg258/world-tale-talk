package com.worldtaletalk.world.repository;

import com.worldtaletalk.statistics.projection.WorldStatsProjection;
import com.worldtaletalk.world.domain.WorldsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WorldsRepository extends JpaRepository<WorldsEntity, Long> {
    // 활성화된 세계관만 조회
    List<WorldsEntity> findAllByActiveTrue();

    // 플레이수를 기준으로 인기 세계관 조회
    @Query("SELECT w FROM WorldsEntity w WHERE w.active = TRUE ORDER BY w.playCount DESC LIMIT :limit")
    List<WorldsEntity> findAllByPlayCount(int limit);

    // 세계관별 완결수와 중도이탈수 그리고 평균 평점을 조회 (통계용)
    @Query("""
    SELECT w.id AS worldId,
           w.name AS worldName,
           COUNT(s.id) AS totalSessions,
           SUM(CASE WHEN s.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completedSessions,
           SUM(CASE WHEN s.status = 'ABANDONED' THEN 1 ELSE 0 END) AS abandonedSessions,
           w.ratingAvg AS ratingAvg
    FROM WorldsEntity w LEFT JOIN StorySessionsEntity s ON s.worlds.id = w.id
    GROUP BY w.id, w.name, w.ratingAvg
    ORDER BY COUNT(s.id) DESC
    """)
    List<WorldStatsProjection> findWorldStats();

    // 활성화된 세계관 단건 조회
    Optional<WorldsEntity> findByIdAndActiveTrue(Long id);
}
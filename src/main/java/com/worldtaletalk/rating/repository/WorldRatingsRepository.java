package com.worldtaletalk.rating.repository;

import com.worldtaletalk.rating.domain.WorldRatingsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorldRatingsRepository extends JpaRepository<WorldRatingsEntity, Long> {
    // 세계관 FK로 전체 조회 (페이징)
    Page<WorldRatingsEntity> findAllByWorldId(Long id, Pageable pageable);

    // 세계관 평점 평균 계산(COALESCE로 null일경우 0.0으로 계산)
    @Query("""
        SELECT COALESCE(AVG(r.score), 0.0) FROM WorldRatingsEntity r
                WHERE r.world.id = :worldId
        """)
    double findAverageScoreByWorldId(@Param("worldId") Long worldId);
}

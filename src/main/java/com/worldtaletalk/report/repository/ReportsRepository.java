package com.worldtaletalk.report.repository;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.domain.ReportTargetType;
import com.worldtaletalk.report.domain.ReportsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReportsRepository extends JpaRepository<ReportsEntity, Long> {
    // 상태별 페이징 처리
    Page<ReportsEntity> findAllByStatus(ReportStatus status, Pageable pageable);

    // 신고 이력 상태별 전체 개수
    long countByStatus(ReportStatus status);

    // 신고 중복 방지
    @Query("""
    SELECT CASE WHEN COUNT(r) > 0 THEN TRUE ELSE FALSE END FROM ReportsEntity r
        WHERE r.users.id = :userId
          AND r.targetType = :targetType
            AND r.targetId = :targetId
    """)
    boolean existsByUserIdAndReport(
            @Param("userId") Long userId,
            @Param("targetType") ReportTargetType targetType,
            @Param("targetId") Long targetId
    );
}

package com.worldtaletalk.report.repository;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.domain.ReportTargetType;
import com.worldtaletalk.report.domain.ReportsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReportsRepository extends JpaRepository<ReportsEntity, Long> {
    // 상태별 페이징 처리
    @Query(value = """
            SELECT r FROM ReportsEntity r
                WHERE (:status IS NULL OR r.status = :status)
            """,
            countQuery = """
            SELECT COUNT(r) FROM ReportsEntity r
                WHERE (:status IS NULL OR r.status = :status)
            """)
    Page<ReportsEntity> findAllByStatus(
            @Param("status") ReportStatus status,
            Pageable pageable);

    // 신고 이력 상태별 전체 개수
    long countByStatus(ReportStatus status);

    // 같은 대상 누적 신고 개수
    long countByTargetTypeAndTargetId(ReportTargetType targetType, Long targetId);

    // 같은 대상 신고 내역 조회
    @Query("""
    SELECT r FROM ReportsEntity r
        JOIN FETCH r.users
            WHERE r.targetType = :targetType
                AND r.targetId = :targetId
            ORDER BY r.createdAt DESC
    """)
    List<ReportsEntity> findAllByTarget(
            @Param("targetType") ReportTargetType targetType,
            @Param("targetId") Long targetId
    );

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

package com.worldtaletalk.report.repository;

import com.worldtaletalk.report.domain.ReportStatus;
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
}

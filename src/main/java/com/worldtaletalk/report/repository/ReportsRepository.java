package com.worldtaletalk.report.repository;

import com.worldtaletalk.report.domain.ReportsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportsRepository extends JpaRepository<ReportsEntity, Long> {
}

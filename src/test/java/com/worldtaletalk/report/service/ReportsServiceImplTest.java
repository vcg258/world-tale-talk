package com.worldtaletalk.report.service;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.domain.ReportTargetType;
import com.worldtaletalk.report.dto.ReportsDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ReportsServiceImplTest {
    @Autowired private ReportsService reportsService;

    @Test
    void getReports() {
        reportsService.getReports(1).forEach(dto -> log.info("{}", dto));
    }

    @Test
    void getReportsByStatus() {
        reportsService.getReportsByStatus(ReportStatus.PENDING, 1).forEach(dto -> log.info("{}", dto));
    }

    @Test
    void addReport() {
        ReportsDTO reportsDTO = ReportsDTO.builder()
                .targetType(ReportTargetType.REVIEW)
                .targetId(2L)
                .reason("test")
                .userId(3L)
                .build();
        reportsService.addReport(reportsDTO);
    }

    @Test
    void handleReport() {
        ReportsDTO reportsDTO = ReportsDTO.builder()
                .id(6L)
                .adminId(1L)
                .status(ReportStatus.RESOLVED)
                .build();
        reportsService.handleReport(reportsDTO);
    }
}
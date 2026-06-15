package com.worldtaletalk.report.service;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.dto.ReportsDTO;

import java.util.List;

public interface ReportsService {

    /**
     * 전체 신고 목록 조회 (관리자용) (페이징)
     * @return 신고 목록
     */
    List<ReportsDTO> getReports();

    /**
     * 처리 상태별 신고 목록 조회 (관리자용) (페이징)
     * @param status 처리 상태 (PENDING, RESOLVED, DISMISSED)
     * @return 상태별 신고 목록
     */
    List<ReportsDTO> getReportsByStatus(ReportStatus status);

    /**
     * 신고 등록
     * @param reportsDTO 신고 정보 (신고자 PK, 대상 종류, 대상 PK, 사유 포함)
     */
    void addReport(ReportsDTO reportsDTO);

    /**
     * 신고 처리 (관리자용 - RESOLVED or DISMISSED)
     * @param id      신고 PK
     * @param adminId 처리한 관리자 PK
     * @param status  처리 결과 (RESOLVED, DISMISSED)
     */
    void handleReport(Long id, Long adminId, ReportStatus status);
}

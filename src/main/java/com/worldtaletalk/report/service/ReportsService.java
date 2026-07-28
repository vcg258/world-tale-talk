package com.worldtaletalk.report.service;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.dto.ReportsDTO;
import org.springframework.data.domain.Page;


public interface ReportsService {
    /**
     * 처리 상태별 신고 목록 조회 (관리자용) (페이징)
     *
     * @param status 처리 상태 (null이면 전체 조회)
     * @return 상태별 신고 목록
     */
    Page<ReportsDTO> getReports(ReportStatus status, int page);

    /**
     * 신고 등록
     *
     * @param reportsDTO 신고 정보
     */
    void addReport(ReportsDTO reportsDTO);

    /**
     * 신고 처리 (관리자용 - RESOLVED or DISMISSED)
     *
     * @param reportsDTO 신고 처리 정보
     */
    void handleReport(ReportsDTO reportsDTO);

    /**
     * 신고된 리뷰 삭제 후 신고 처리 완료
     *
     * @param reportId 신고 PK
     * @param adminId 관리자 PK
     */
    void deleteReportedReview(Long reportId, Long adminId);

    /**
     * 신고 내역 삭제
     *
     * @param id PK
     */
    void deleteReport(Long id);
}

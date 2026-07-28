package com.worldtaletalk.report.dto;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.domain.ReportTargetType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportsDTO {
    private Long id;
    private Long userId; // 신고한 유저 FK
    private Long adminId; // 처리할 관리자 FK (처리전 null)
    private String adminNickname; // 처리한 관리자 닉네임
    private ReportTargetType targetType; // 신고 대상 종류
    private Long targetId; // 신고 대상 PK
    private String reason; // 신고 사유
    private ReportStatus status; // 처리 상태
    private LocalDateTime handledAt; // 처리 일시 (처리전 null)
    private String targetText; // 관리자 화면용 신고 대상 문구
    private String targetLink; // 관리자 화면용 신고 대상 확인 링크
    private Long targetSessionId; // 채팅 신고 대상이 속한 세션 PK
    private long cumulativeReportCount; // 같은 대상 누적 신고 수
    private List<ReportHistoryDTO> histories; // 같은 대상 신고 내역
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

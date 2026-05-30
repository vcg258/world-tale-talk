package com.worldtaletalk.report.dto;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.domain.ReportTargetType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportsDTO {
    private Long id;
    private Long userId; // 신고한 유저 FK
    private Long adminId; // 처리할 관리자 FK (처리전 null)
    private ReportTargetType targetType; // 신고 대상 종류
    private Long targetId; // 신고 대상 PK
    private String reason; // 신고 사유
    private ReportStatus status; // 처리 상태
    private LocalDateTime handledAt; // 처리 일시 (처리전 null)

}

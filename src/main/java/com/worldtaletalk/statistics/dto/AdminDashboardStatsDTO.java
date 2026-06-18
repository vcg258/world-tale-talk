package com.worldtaletalk.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStatsDTO {
    private long userCount; // 전체 회원 수
    private long worldCount; // 전체 세계관 수
    private long activeSessionCount; // 진행 중 세션 수
    private long pendingReportCount; // 처리 대기 신고 수
}

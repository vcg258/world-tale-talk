package com.worldtaletalk.report.dto;

import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.domain.ReportTargetType;
import com.worldtaletalk.user.dto.UsersDTO;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportsDTO {
    private Long id;
    private UsersDTO users; // 신고한 유저
    private UsersDTO admins; // 처리할 관리자 (처리전 null)
    private ReportTargetType targetType;
    private Long targetId;
    private String reason;
    private ReportStatus status;
    private LocalDateTime handledAt; // 처리 일시 (처리전 null)

}

package com.worldtaletalk.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportHistoryDTO {
    private Long id;
    private String reporterNickname;
    private String reason;
    private LocalDateTime createdAt;
}

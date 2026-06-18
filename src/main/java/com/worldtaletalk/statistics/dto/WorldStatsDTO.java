package com.worldtaletalk.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorldStatsDTO {
    private Long worldId;
    private String worldName;
    private Long totalSessions; // 전체 플레이 수
    private Long completedSessions; // 완결 수
    private Long abandonedSessions; // 포기 수
    private BigDecimal ratingAvg; // 평균 평점
}

package com.worldtaletalk.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDetailedStatsDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalStories;
    private long endingCount;
    private long abandonedCount;
    private long activeCount;
    private List<UsageStatsItemDTO> worldUsage;
    private List<UsageStatsItemDTO> personaUsage;
    private List<UsageStatsItemDTO> interestTargets;
    private List<TimeSeriesStatsDTO> timeSeries;
}

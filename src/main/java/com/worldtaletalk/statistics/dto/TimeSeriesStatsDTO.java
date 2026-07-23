package com.worldtaletalk.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesStatsDTO {
    private LocalDate date;
    private long totalStories;
    private long endingCount;
    private long abandonedCount;
    private long activeCount;
}

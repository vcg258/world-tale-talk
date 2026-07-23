package com.worldtaletalk.statistics.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UsageStatsItemDTO {
    private String label;
    private long count;
    private double ratio;
    private String groupLabel;

    public UsageStatsItemDTO(String label, long count, double ratio) {
        this(label, count, ratio, null);
    }

    public UsageStatsItemDTO(String label, long count, double ratio, String groupLabel) {
        this.label = label;
        this.count = count;
        this.ratio = ratio;
        this.groupLabel = groupLabel;
    }
}

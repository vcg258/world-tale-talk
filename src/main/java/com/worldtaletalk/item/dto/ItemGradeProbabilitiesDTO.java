package com.worldtaletalk.item.dto;

import com.worldtaletalk.item.domain.ItemGrade;
import com.worldtaletalk.item.domain.itemgradeprobabilities.PhaseType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemGradeProbabilitiesDTO {
    private Long id;
    private PhaseType phase; // 진행도 구간
    private ItemGrade grade; // 아이템 등급
    private int probability; // 아이템 등급 확률 (동일 구간 합산 100으로 정함)
}

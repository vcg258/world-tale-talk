package com.worldtaletalk.item.dto;

import com.worldtaletalk.item.domain.ItemGrade;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemGradeConfigsDTO {
    private Long id;
    private ItemGrade grade; // 아이템 등급
    private int statSumMax; // 스텟 보정 최대치
    private int singleMax; // 단일 스텟 보정 최대치
    private int singleMin; // 단일 스텟 보정 최소치
    private boolean canNegative; // 음수 보정 허용여부 (True = 음수 허용)
}

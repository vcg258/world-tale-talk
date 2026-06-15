package com.worldtaletalk.item.dto;

import com.worldtaletalk.item.domain.ItemGrade;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemGradeConfigsDTO {
    private Long id;
    private ItemGrade grade; // 아이템 등급
    private Integer statSumMax; // 스텟 보정 최대치
    private Integer singleMax; // 단일 스텟 보정 최대치
    private Integer singleMin; // 단일 스텟 보정 최소치
    private Boolean canNegative; // 음수 보정 허용여부 (True = 음수 허용)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

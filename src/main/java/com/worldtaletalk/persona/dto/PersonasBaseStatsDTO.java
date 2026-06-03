package com.worldtaletalk.persona.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonasBaseStatsDTO {
    private Long id;
    private Long personaId; // 페르소나 FK
    private Integer wisdom; // 지혜
    private Integer strength; // 힘
    private Integer agility; // 민첩
    private Integer charisma; // 카리스마
    private Integer luck; // 행운
}
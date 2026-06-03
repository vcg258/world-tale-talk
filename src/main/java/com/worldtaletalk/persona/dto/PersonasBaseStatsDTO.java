package com.worldtaletalk.persona.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonasBaseStatsDTO {
    private Long id;
    private Long personaId; // 페르소나 FK
    private int wisdom; // 지혜
    private int strength; // 힘
    private int agility; // 민첩
    private int charisma; // 카리스마
    private int luck; // 행운
}
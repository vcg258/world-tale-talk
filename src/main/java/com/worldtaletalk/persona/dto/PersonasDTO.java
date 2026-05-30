package com.worldtaletalk.persona.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonasDTO{
    private Long id;
    private Long worldId; // 세계관 FK
    private String name; // 페르소나 이름
    private String description; // 페르소나 소개
    private String background; // 페르소나 배경(LLM 주입용)
    private boolean isActive; // 활성화 여부 (True 활성화)
}

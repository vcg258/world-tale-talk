package com.worldtaletalk.session.dto;

import com.worldtaletalk.session.domain.EndingType;
import com.worldtaletalk.session.domain.SessionStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorySessionsDTO {
    private Long id;
    private Long userId; // 플레이 유저 FK
    private Long worldId; // 선택한 세계관 FK
    private Long personaId; // 선택한 페르소나 FK
    private SessionStatus status; // 세션 상태
    private int currentTurn; // 현재 턴 수
    private EndingType endingType; // 도달한 엔딩 유형
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
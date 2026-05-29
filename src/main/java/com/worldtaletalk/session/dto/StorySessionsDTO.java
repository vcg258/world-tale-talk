package com.worldtaletalk.session.dto;

import com.worldtaletalk.persona.dto.PersonasDTO;
import com.worldtaletalk.session.domain.EndingType;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.user.dto.UsersDTO;
import com.worldtaletalk.world.dto.WorldsDTO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorySessionsDTO {
    private Long id;
    private UsersDTO user; // 플레이 유저 FK (DTO 변경예정)
    private WorldsDTO world; // 선택한 세계관 FK (DTO 변경예정)
    private PersonasDTO persona; // 선택한 페르소나 FK (DTO 변경예정)
    private SessionStatus status; // 세션 상태
    private int currentTurn; // 현재 턴 수
    private EndingType endingType; // 도달한 엔딩 유형
}
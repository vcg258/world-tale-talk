package com.worldtaletalk.stat.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatSnapshotsDTO {
    private Long id;
    private Long storySessionId; // 세션 FK
    private int wisdom; // 엔딩 시점 지혜
    private int strength; // 엔딩 시점 힘
    private int agility; // 엔딩 시점 민첩
    private int charisma; // 엔딩 시점 카리스마
    private int luck; // 엔딩 시점 행운
    private String storySummary; // 엔딩 시점 메인 스토리 요약본
}

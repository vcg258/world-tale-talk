package com.worldtaletalk.stat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatLogsDTO {
    private Long id;
    private Long storySessionId; // 세션 FK
    private int turn; // 스텟 변화가 발생한 턴
    private int wisdomDelta; // 지혜 변화량
    private int strengthDelta; // 힘 변화량
    private int agilityDelta; // 민첩 변화량
    private int charismaDelta; // 카리스마 변화량
    private int luckDelta; // 행운 변화량
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

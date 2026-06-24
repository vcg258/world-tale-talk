package com.worldtaletalk.story.dto;

import com.worldtaletalk.inventory.dto.SessionItemsDTO;
import com.worldtaletalk.session.domain.EndingType;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.stat.dto.SessionStatSnapshotsDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 서버 턴 처리 결과 (서버 내부 전용 - 클라이언트 직접 노출 없음)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnProcessResult {
    private StorySessionsDTO session; // 턴/상태 갱신된 세션
    private SessionStatLogsDTO appliedStatLog; // 적용된 스탯 변화 로그
    private SessionItemsDTO createdItem; // 생성된 아이템 (null = 없음)
    private Long removedItemId; // 소실된 아이템 PK (null = 없음)
    private EndingType endingType; // 서버 판별 엔딩 유형 (null = 없음)
    private SessionStatSnapshotsDTO snapshot; // 엔딩 시점 스탯 스냅샷 (null = 없음)
    private String story; // LLM 생성 소설 본문
    private List<String> choices; // 선택지 3가지 (null = 엔딩 도달)
}

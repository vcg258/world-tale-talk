package com.worldtaletalk.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

// LLM Structured Output 파싱 결과 (서버 내부 전용 - 클라이언트 직접 노출 없음)
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmTurnResult {
    private String story; // 소설 본문
    private List<String> choices; // 선택지 3개
    private Map<String, Integer> statChanges; // 스텟 변화량 (자유 입력 일경우 선택지를 선택할 경우 빈 Map으로 반환 하도록 함)
    private Map<String, Map<String, Integer>> statChangesByChoice; // 스탯 변화량 (선택지 별 Redis에 캐싱) ex) {"choice1": {"wisdom": 2, "strength": -1}, "choice2": {...}, "choice3": {...}}
    private LlmItemEvent itemEvent; // 아이템 이벤트 (null은 아이템 흭득 없음)
    private boolean endingFlag; // 엔딩 조건 충족 여부
}

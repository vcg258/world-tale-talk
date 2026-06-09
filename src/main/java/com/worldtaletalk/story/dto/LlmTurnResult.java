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
    private Map<String, Integer> statChanges; // 스탯 변화량
    private LlmItemEvent itemEvent; // 아이템 이벤트 (null은 아이템 흭득 없음)
    private boolean endingFlag; // 엔딩 조건 충족 여부
}

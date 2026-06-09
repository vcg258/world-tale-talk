package com.worldtaletalk.story.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// LLM 이 생성한 아이템 이벤트
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmItemEvent {
    private ItemEventType type; // 아이템 이벤트 타입
    private String itemName; // LLM 생성 아이템 이름
    private String itemDescription; // LLM 생성 아이템 설명
    private ItemContext itemContext; // 등급 힌트
}

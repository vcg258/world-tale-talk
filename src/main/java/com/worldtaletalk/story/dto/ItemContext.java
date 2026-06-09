package com.worldtaletalk.story.dto;

// LLM 아이템 등급 힌트 (최종 등급은 서버에서 결정함)
public enum ItemContext {
    MINOR, // 잡템 - COMMON
    NORMAL, // 일반 - RARE
    SIGNIFICANT, // 희귀템 - EPIC
    LEGENDARY // 전설템 - CURSED
}

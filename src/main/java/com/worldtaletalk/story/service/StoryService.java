package com.worldtaletalk.story.service;

import com.worldtaletalk.story.dto.TurnProcessResult;

public interface StoryService {

    /**
     * 스토리 진행 (유저 입력 처리 총괄)
     * - RAG 검색
     * - LLM 호출
     * - 스탯/아이템 처리
     * - 채팅 메시지 저장
     * - 엔딩 조건 판별
     */
    TurnProcessResult processStory(Long sessionId, String userInput);
}

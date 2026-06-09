package com.worldtaletalk.story.service;

public interface StoryService {

    /**
     * 스토리 진행 (유저 입력 처리 총괄)
     * - RAG 검색
     * - LLM 호출
     * - 스탯/아이템 처리
     * - 채팅 메시지 저장
     * - 엔딩 조건 판별
     */
    void processStory(Long sessionId, String userInput);
}

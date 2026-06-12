package com.worldtaletalk.story.service;

import com.worldtaletalk.story.dto.TurnProcessResult;

public interface StoryService {
    /**
     * LLM 호출후 스텟 / 아이템 / 엔딩 / 턴수 처리
     * @param sessionId 세션 PK
     * @param userInput 유저 프롬프트
     * @param choiceNumber 선택지 선택
     * @return 로직 처리된 세션 정보
     */
    TurnProcessResult processStory(Long sessionId, String userInput, Integer choiceNumber);
}

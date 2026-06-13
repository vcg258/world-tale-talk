package com.worldtaletalk.story.service;

import com.worldtaletalk.story.dto.LlmTurnResult;

public interface StoryAiService {

    /**
     * 세계관 삭제
     * @param worldId 세계관 PK
     */
    void deleteWorldIndex(Long worldId);

    /**
     * 세계관 등록/수정
     * @param worldId 세계관 PK
     */
    void indexWorld(Long worldId);

    /**
     * 스토리 생성 (RAG + LLM 호출)
     * @param sessionId 세션 PK
     * @param userInput 유저 입력
     * @return LLM 응답 파싱 결과
     */
    LlmTurnResult generateStory(Long sessionId, String userInput);
}

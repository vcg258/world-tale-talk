package com.worldtaletalk.story.service;

import com.worldtaletalk.story.dto.LlmTurnResult;
import reactor.core.publisher.Flux;

public interface StoryAiService {

    /**
     * 세계관 삭제
     *
     * @param worldId 세계관 PK
     */
    void deleteWorldIndex(Long worldId);

    /**
     * 세계관 등록/수정
     *
     * @param worldId 세계관 PK
     */
    void indexWorld(Long worldId);

    /**
     * 스토리 생성 (RAG + LLM 호출 + 스트리밍 기능)
     *
     * @param sessionId 세션 PK
     * @param userInput 유저 입력
     * @return LLM 응답 파싱 결과
     */
    Flux<String> streamNarrative(Long sessionId, String userInput);

    /**
     * 스토리 메타데이터 생성
     *
     * @param sessionId 세션 PK
     * @param userInput 유저 입력
     * @param narrative streamNarrative가 생성한 서사 텍스트 전문
     * @return 선택지·스탯 변화·아이템 이벤트 등이 담긴 LlmTurnResult
     */
    LlmTurnResult generateMetadata(Long sessionId, String userInput, String narrative);

    /**
     * 메인 스토리 이후 에필로그 스토리 생성 (스트리밍 기능)
     *
     * @param sessionId 세션 PK
     * @param userInput 유저 입력
     * @return LLM 응답 결과 (String)
     */
    Flux<String> streamEpilogue(Long sessionId, String userInput);

    /**
     * 메인 스토리 요약 지정 턴마다 갱신
     *
     * @param sessionId 세션 PK
     * @return 메인 스토리 요약
     */
    String generateRunningSummary(Long sessionId);

    /**
     * 엔딩 이후 메인스토리 요약
     *
     * @param sessionId 세션 PK
     * @return 메인스토리 요약
     */
    String modifyMainSummary(Long sessionId);
}

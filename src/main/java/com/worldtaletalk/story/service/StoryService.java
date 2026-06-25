package com.worldtaletalk.story.service;

import com.worldtaletalk.story.dto.TurnProcessResult;

import java.util.function.Consumer;

public interface StoryService {

    /**
     * 메인 스토리 스트리밍 처리
     *
     * @param sessionId       세션 PK
     * @param userInput       유저 프롬프트
     * @param choiceNumber    선택지 번호 (자유 입력 시 null)
     * @param chunkConsumer   서사 토큰 수신 콜백
     * @param completeConsumer 턴 완료 시 결과 전달 콜백
     * @param errorConsumer   오류 발생 시 예외 전달 콜백
     */
    void processStoryStream(Long sessionId, String userInput, Integer choiceNumber,
                            Consumer<String> chunkConsumer,
                            Consumer<TurnProcessResult> completeConsumer,
                            Consumer<Exception> errorConsumer);

    /**
     * 에필로그 스트리밍 처리
     *
     * @param sessionId     세션 PK
     * @param userInput     유저 프롬프트
     * @param chunkConsumer 에필로그 토큰 수신 콜백
     * @param errorConsumer 오류 발생 시 예외 전달 콜백
     */
    void processEpilogueStream(Long sessionId, String userInput,
                               Consumer<String> chunkConsumer,
                               Consumer<Exception> errorConsumer);
}

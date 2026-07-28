package com.worldtaletalk.story.service;

import com.worldtaletalk.chat.service.ChatMessagesService;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.stat.service.SessionStatsService;
import com.worldtaletalk.story.cache.StoryCacheService;
import com.worldtaletalk.story.dto.LlmTurnResult;
import com.worldtaletalk.story.dto.PlayStateCacheDTO;
import com.worldtaletalk.story.dto.TurnProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {
    private final StoryAiService storyAiService;
    private final StoryTurnTransactionService storyTurnTransactionService;
    private final SessionStatsService sessionStatsService;
    private final StoryCacheService storyCacheService;
    private final StorySessionsService storySessionsService;
    private final ChatMessagesService chatMessagesService;

    /**
     * 메인 스토리 스트리밍 처리
     *
     * @param sessionId        세션 PK
     * @param userInput        유저 프롬프트
     * @param choiceNumber     선택지 번호 (자유 입력 시 null)
     * @param chunkConsumer    서사 토큰 수신 콜백
     * @param completeConsumer 턴 완료 시 결과 전달 콜백
     * @param errorConsumer    오류 발생 시 예외 전달 콜백
     */
    @Override
    public void processStoryStream(Long sessionId, String userInput, Integer choiceNumber,
                                   Consumer<String> chunkConsumer,
                                   Consumer<TurnProcessResult> completeConsumer,
                                   Consumer<Exception> errorConsumer
    ) {
        flushCurrentStoryToHistory(sessionId);

        // 트랜잭션 1: 턴 시작 (선택지 스탯)
        SessionStatLogsDTO statLogsDTO = storyTurnTransactionService.beginTurn(sessionId, choiceNumber);
        int turn = storySessionsService.getStorySession(sessionId).getCurrentTurn();
        chatMessagesService.saveUserInput(sessionId, turn, userInput);
        StringBuilder narrativeBuilder = new StringBuilder();

        storyAiService.streamNarrative(sessionId, userInput)
                .subscribe(
                        chunk -> {
                            narrativeBuilder.append(chunk); // 넘어온 청크별로 자름
                            chunkConsumer.accept(chunk);
                        },
                        // 서사 스트리밍 자체 오류 -> 보상 트랜잭션
                        e -> {
                            storyTurnTransactionService.compensateTurn(sessionId, statLogsDTO); // LLM호출 실패시 롤백
                            errorConsumer.accept((Exception) e);
                        },
                        // 서사 스트리밍 완료 -> 블로킹 후속 작업은 boundedElastic에서 실행
                        () -> Schedulers.boundedElastic().schedule(() -> {
                            TurnProcessResult result = null;

                            try {
                                // LLM 2차 호출: 메타데이터 (선택지, 스탯 변화 등)
                                LlmTurnResult llmTurnResult = storyAiService.generateMetadata(
                                        sessionId, userInput, narrativeBuilder.toString());

                                // 트랜잭션 2: 턴 완료 (스탯 로그, 아이템, 엔딩 처리)
                                result = storyTurnTransactionService.completeTurn(
                                        sessionId, choiceNumber, statLogsDTO, llmTurnResult);
                            } catch (Exception e) { // 예외 발생시 롤백 (보상 처리)
                                storyTurnTransactionService.compensateTurn(sessionId, statLogsDTO);
                                errorConsumer.accept(e);
                                return;
                            }

                            try {
                                // 엔딩 도달 시 메인 스토리 요약 저장
                                if (result.getEndingType() != null) {
                                    String summary = storyAiService.modifyMainSummary(sessionId);
                                    sessionStatsService.modifySummary(sessionId, summary);
                                    log.info("메인스토리 요약 저장 완료 sessionId: {}", sessionId);
                                }
                                // 지정 턴 마다 스토리 요약본 갱신
                                if (result.getEndingType() == null && result.getSession().getCurrentTurn() % 5 == 0) {
                                    String runningSummary = storyAiService.generateRunningSummary(sessionId);
                                    storySessionsService.modifyRunningSummary(sessionId, runningSummary);
                                    storyCacheService.saveRunningSummary(sessionId, runningSummary);
                                    log.info("런닝 요약 갱신 sessionId: {}, turn: {}", sessionId, result.getSession().getCurrentTurn());
                                }
                            } catch (Exception e) {
                                log.error("요약 생성 실패 (턴 처리 자체는 정상 커밋됨) sessionId: {}", sessionId, e);
                            }

                            completeConsumer.accept(result);
                        })
                );
    }


    private void flushCurrentStoryToHistory(Long sessionId) {
        PlayStateCacheDTO playState = storyCacheService.getPlayState(sessionId);
        if (playState == null || playState.getCurrentStory() == null || playState.getCurrentStory().isBlank()) {
            return;
        }

        int turn = storySessionsService.getStorySession(sessionId).getCurrentTurn();
        chatMessagesService.saveStoryExchange(sessionId, turn, playState.getUserInput(), playState.getCurrentStory());
        storyCacheService.deletePlayState(sessionId);
        log.info("현재 장면 채팅 기록 확정 저장 sessionId: {}, turn: {}", sessionId, turn);
    }

    /**
     * 에필로그 스트리밍 처리
     *
     * @param sessionId     세션 PK
     * @param userInput     유저 프롬프트
     * @param chunkConsumer 에필로그 토큰 수신 콜백
     * @param completeConsumer 스트리밍 정상 종료 콜백 (구조화된 결과가 없어 Runnable)
     * @param errorConsumer 오류 발생 시 예외 전달 콜백
     */
    @Override
    public void processEpilogueStream(Long sessionId, String userInput,
                                      Consumer<String> chunkConsumer,
                                      Runnable completeConsumer,
                                      Consumer<Exception> errorConsumer
    ) {
        storyAiService.streamEpilogue(sessionId, userInput)
                .subscribe(
                        chunkConsumer,
                        e -> {
                            log.error("에필로그 스트리밍 실패 sessionId: {}", sessionId, e);
                            errorConsumer.accept((Exception) e);
                        },
                        completeConsumer
                );
    }
}

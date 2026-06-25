package com.worldtaletalk.story.service;

import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.stat.service.SessionStatsService;
import com.worldtaletalk.story.dto.LlmTurnResult;
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
        // 트랜잭션 1: 턴 시작 (선택지 스탯)
        SessionStatLogsDTO statLogsDTO = storyTurnTransactionService.beginTurn(sessionId, choiceNumber);
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
                            try {
                                // LLM 2차 호출: 메타데이터 (선택지, 스탯 변화 등)
                                LlmTurnResult llmTurnResult = storyAiService.generateMetadata(
                                        sessionId, userInput, narrativeBuilder.toString());

                                // 트랜잭션 2: 턴 완료 (스탯 로그, 아이템, 엔딩 처리)
                                TurnProcessResult result = storyTurnTransactionService.completeTurn(
                                        sessionId, choiceNumber, statLogsDTO, llmTurnResult);

                                // 엔딩 도달 시 메인 스토리 요약 저장
                                if (result.getEndingType() != null) {
                                    String summary = storyAiService.modifyMainSummary(sessionId);
                                    sessionStatsService.modifySummary(sessionId, summary);
                                    log.info("메인스토리 요약 저장 완료 sessionId: {}", sessionId);
                                }

                                completeConsumer.accept(result);
                            } catch (Exception e) {
                                storyTurnTransactionService.compensateTurn(sessionId, statLogsDTO);
                                errorConsumer.accept(e);
                            }
                        })
                );
    }

    /**
     * 에필로그 스트리밍 처리
     *
     * @param sessionId     세션 PK
     * @param userInput     유저 프롬프트
     * @param chunkConsumer 에필로그 토큰 수신 콜백
     * @param errorConsumer 오류 발생 시 예외 전달 콜백
     */
    @Override
    public void processEpilogueStream(Long sessionId, String userInput,
                                      Consumer<String> chunkConsumer,
                                      Consumer<Exception> errorConsumer
    ) {
        storyAiService.streamEpilogue(sessionId, userInput)
                .subscribe(
                        chunkConsumer,
                        e -> {
                            log.error("에필로그 스트리밍 실패 sessionId: {}", sessionId, e);
                            errorConsumer.accept((Exception) e);
                        }
                );
    }
}

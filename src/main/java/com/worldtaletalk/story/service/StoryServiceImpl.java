package com.worldtaletalk.story.service;

import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.story.dto.LlmTurnResult;
import com.worldtaletalk.story.dto.TurnProcessResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

@Log4j2
@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {
    private final StoryAiService storyAiService;
    private final StoryTurnTransactionService storyTurnTransactionService;

    /**
     * LLM 호출후 스텟 / 아이템 / 엔딩 / 턴수 처리 (LLM호출 실패했을시 트랜잭션 적용을 위해 기능따로 분리)
     * @param sessionId 세션 PK
     * @param userInput 유저 프롬프트
     * @return 로직 처리된 세션 정보
     */
    @Override
    public TurnProcessResult processStory(Long sessionId, String userInput, Integer choiceNumber) {

        // 트랜잭션 1
        SessionStatLogsDTO statLogsDTO = storyTurnTransactionService.beginTurn(sessionId, choiceNumber);

        // LLM 호출
        LlmTurnResult llmTurnResult;
        try {
            llmTurnResult = storyAiService.generateStory(sessionId, userInput);
        } catch (Exception e) {
            storyTurnTransactionService.compensateTurn(sessionId, statLogsDTO);
            throw new IllegalStateException("스토리 생성 실패 턴 롤백", e);
        }

        // 트랜잭션 2
        return storyTurnTransactionService.completeTurn(sessionId, choiceNumber, statLogsDTO, llmTurnResult);
    }

}

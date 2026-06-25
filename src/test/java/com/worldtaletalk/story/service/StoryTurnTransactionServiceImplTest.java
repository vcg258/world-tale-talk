package com.worldtaletalk.story.service;

import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.story.dto.ItemContext;
import com.worldtaletalk.story.dto.ItemEventType;
import com.worldtaletalk.story.dto.LlmItemEvent;
import com.worldtaletalk.story.dto.LlmTurnResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
class StoryTurnTransactionServiceImplTest {
    @Autowired private StoryTurnTransactionService storyTurnTransactionService;

    @Test
    void beginTurn() {
        storyTurnTransactionService.beginTurn(7L, 1);
    }

    @Test
    void completeTurn() {
        SessionStatLogsDTO log = SessionStatLogsDTO.builder()
                .storySessionId(7L)
                .turn(2)
                .wisdomDelta(0)
                .strengthDelta(0)
                .agilityDelta(0)
                .charismaDelta(1)
                .luckDelta(0)
                .build();

        LlmTurnResult result = LlmTurnResult.builder()
                .story("스토리 본문")
                .choices(List.of("choices1", "choices2", "choices3"))
                .statChanges(Map.of()) // 선택지 입력이면 빈 맵
                .statChangesByChoice(Map.of(
                        "choice1", Map.of("wisdom", 1),
                        "choice2", Map.of("agility", 1),
                        "choice3", Map.of("charisma", 1)
                ))
                .itemEvent(LlmItemEvent.builder()
                        .type(ItemEventType.LOSE)
                        .itemName("실제 보유 아이템 이름1")
                        .itemDescription("설명")
                        .itemContext(ItemContext.MINOR)
                        .build())
                .endingFlag(false)
                .hiddenEventFlag(false)
                .build();
        storyTurnTransactionService.completeTurn(7L, 1, log, result);
    }

    @Test
    void compensateTurn() {
        SessionStatLogsDTO log = SessionStatLogsDTO.builder()
                .id(7L)
                .storySessionId(7L)
                .turn(2)
                .wisdomDelta(0)
                .strengthDelta(0)
                .agilityDelta(0)
                .charismaDelta(1)
                .luckDelta(0)
                .build();
        storyTurnTransactionService.compensateTurn(7L, log);
    }
}
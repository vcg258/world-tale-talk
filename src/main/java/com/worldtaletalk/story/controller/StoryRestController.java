package com.worldtaletalk.story.controller;

import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.stat.dto.SessionStatLogsDTO;
import com.worldtaletalk.story.dto.TurnProcessResult;
import com.worldtaletalk.story.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class StoryRestController {

    private final StoryService storyService;

    private record TurnRequest(String content, Integer choiceIndex) {}

    private record TurnResponse(
            int turn,
            String story,
            List<String> choices,
            Map<String, Integer> statChanges,
            boolean endingTriggered,
            SessionStatus sessionStatus
    ) {}

    private record EpilogueRequest(String content) {}
    private record EpilogueResponse(String story) {}

    /**
     * S-01: 메인 스토리 턴 진행
     *
     * @param sessionId 세션 PK
     * @param request   유저 입력 텍스트
     * @return 플레이 정보 (턴, 스텟, 아이템 등)
     */
    @PostMapping("/{sessionId}/turn")
    public ResponseEntity<TurnResponse> turn(@PathVariable Long sessionId, @RequestBody TurnRequest request) {
        // 스토리 진행
        TurnProcessResult result = storyService.processStory(sessionId, request.content(), request.choiceIndex());

        // 스탯 변화량 로그
        SessionStatLogsDTO log = result.getAppliedStatLog();
        Map<String, Integer> statChanges = log != null ?
                Map.of(
                        "wisdom", log.getWisdomDelta(),
                        "strength", log.getStrengthDelta(),
                        "agility", log.getAgilityDelta(),
                        "charisma", log.getCharismaDelta(),
                        "luck", log.getLuckDelta()
                ) : Map.of();
        return ResponseEntity.ok(new TurnResponse(
                result.getSession().getCurrentTurn(),
                result.getStory(),
                result.getChoices(),
                statChanges,
                result.getEndingType() != null,
                result.getSession().getStatus()
        ));
    }

    /**
     * S-02: 에필로그 진행
     *
     * @param sessionId 세션 PK
     * @param request   유저 입력 텍스트
     * @return 에필로그 소설 본문
     */
    @PostMapping("/{sessionId}/epilogue")
    public ResponseEntity<EpilogueResponse> epilogue(@PathVariable Long sessionId, @RequestBody EpilogueRequest request) {
        String result = storyService.processEpilogue(sessionId, request.content());
        return ResponseEntity.ok(new EpilogueResponse(result));
    }
}

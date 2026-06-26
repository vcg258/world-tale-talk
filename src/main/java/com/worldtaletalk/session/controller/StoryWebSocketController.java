package com.worldtaletalk.session.controller;

import com.worldtaletalk.story.service.StoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.messaging.handler.annotation.Payload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StoryWebSocketController {

    private final StoryService storyService;
    private final SimpMessagingTemplate messagingTemplate;

    // 메인 스토리 턴 요청 메시지 - 선택지 번호 입력와 자유 입력 두 타입을 하나의 레코드로 처리함
    private record StoryInputMessage(
            @NotNull(message = "type은 필수입니다.")
            String type,            // 선택지와 자유입력
            @Size(max = 500, message = "입력값은 500자 이내여야 합니다.")
            String content,         // 자유 입력일 때
            Integer choiceIndex     // 선택지일 때
    ) {
    }

    // 에필로그 턴 요청 메시지 - 자유 입력만 허용
    private record EpilogueInputMessage(
            @NotBlank(message = "입력값은 필수입니다.")
            @Size(max = 500, message = "입력값은 500자 이내여야 합니다.")
            String content
    ) {
    }

    // WebSocket 공용 응답 - 스트리밍 청크, 턴 완료 결과, 오류 세 이벤트를 하나의 레코드로 처리함
    private record StoryMessageResponse(
            String event,   // DELTA / COMPLETE / ERROR (웹소켓 이벤트 타입)
            String field,   // DELTA 시 "story"
            String value,   // DELTA 청크 텍스트 / ERROR 메시지
            Object result   // COMPLETE 시 TurnProcessResult
    ) {
    }

    /**
     * WS-00: 스토리 시작 (첫 서사 생성)
     *
     * @param sessionId 세션 PK
     */
    @MessageMapping("/story/{sessionId}/start")
    public void startStory(@DestinationVariable Long sessionId) {
        String destination = "/topic/story/" + sessionId;
        log.info("스토리 시작 요청 sessionId: {}", sessionId);
        storyService.processStoryStream(
                sessionId, "이야기를 시작해주세요.", null,
                chunk -> messagingTemplate.convertAndSend(
                        destination, new StoryMessageResponse("DELTA", "story", chunk, null)),
                result -> messagingTemplate.convertAndSend(
                        destination, new StoryMessageResponse("COMPLETE", null, null, result)),
                e -> {
                    log.error("스토리 시작 실패 sessionId: {}", sessionId, e);
                    messagingTemplate.convertAndSend(
                            destination, new StoryMessageResponse("ERROR", null, e.getMessage(), null));
                }
        );
    }

    /**
     * WS-01: 메인 스토리 턴 처리
     *
     * @param sessionId 세션 PK
     * @param message   유저 입력 (CHOICE / FREE_TEXT)
     */
    @MessageMapping("/story/{sessionId}")
    public void processTurn(@DestinationVariable Long sessionId,
                            @Valid @Payload StoryInputMessage message
    ) {
        String destination = "/topic/story/" + sessionId;

        log.info("스토리 턴 요청 sessionId: {} type: {}", sessionId, message.type());

        storyService.processStoryStream(
                sessionId,
                message.content(),
                message.choiceIndex(),
                chunk -> messagingTemplate.convertAndSend(
                        destination, new StoryMessageResponse("DELTA", "story", chunk, null)),
                result -> messagingTemplate.convertAndSend(
                        destination, new StoryMessageResponse("COMPLETE", null, null, result)),
                e -> {
                    log.error("스토리 턴 처리 실패 sessionId: {}", sessionId, e);
                    messagingTemplate.convertAndSend(
                            destination, new StoryMessageResponse("ERROR", null, e.getMessage(), null));
                }
        );
    }

    /**
     * WS-02: 에필로그 턴 처리
     *
     * @param sessionId 세션 PK
     * @param message   유저 입력
     */
    @MessageMapping("/epilogue/{sessionId}")
    public void processEpilogue(@DestinationVariable Long sessionId,
                                @Valid @Payload EpilogueInputMessage message
    ) {
        String destination = "/topic/epilogue/" + sessionId;

        log.info("에필로그 턴 요청 sessionId: {} content: {}", sessionId, message.content());

        storyService.processEpilogueStream(
                sessionId,
                message.content(),
                chunk -> messagingTemplate.convertAndSend(
                        destination, new StoryMessageResponse("DELTA", "story", chunk, null)),
                e -> {
                    log.error("에필로그 처리 실패 sessionId: {}", sessionId, e);
                    messagingTemplate.convertAndSend(
                            destination, new StoryMessageResponse("ERROR", null, e.getMessage(), null));
                }
        );
    }
}

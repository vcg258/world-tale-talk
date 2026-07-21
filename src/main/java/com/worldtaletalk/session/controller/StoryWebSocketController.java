package com.worldtaletalk.session.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.moderation.ChatModerationService;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.story.cache.StoryCacheService;
import com.worldtaletalk.story.dto.PlayStateCacheDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StoryWebSocketController {

    private final StoryService storyService;
    private final SimpMessagingTemplate messagingTemplate;
    private final StoryCacheService storyCacheService;
    private final StorySessionsService storySessionsService;
    private final ChatModerationService chatModerationService;

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
     * @param user      현재 로그인 유저
     */
    @MessageMapping("/story/{sessionId}/start")
    public void startStory(@DestinationVariable Long sessionId,
                           @AuthenticationPrincipal AuthenticatedUser user) {
        String destination = "/topic/story/" + sessionId;

        if (isForbidden(sessionId, user, destination)) {
            return;
        }

        log.info("스토리 시작 요청 sessionId: {}", sessionId);
        storyService.processStoryStream(
                sessionId, "이야기를 시작해주세요.", null,
                chunk -> messagingTemplate.convertAndSend(
                        destination, new StoryMessageResponse("DELTA", "story", chunk, null)),
                result -> {
                    // 엔딩 도달 레디스에 본문 저장
                    if (result.getEndingType() != null) {
                        storyCacheService.saveEndingStory(sessionId, result.getStory());

                    // 유효 턴 완료 레디스에 상태 저장
                    } else if (result.getChoices() != null && !result.getChoices().isEmpty()) {
                        storyCacheService.savePlayState(
                                sessionId,
                                PlayStateCacheDTO.builder()
                                        .currentStory(result.getStory())
                                        .userInput(null)
                                        .choices(result.getChoices())
                                        .progressPercent(result.getProgressPercent())
                                        .build()
                        );
                    }
                    messagingTemplate.convertAndSend(
                            destination, new StoryMessageResponse("COMPLETE", null, null, result));
                },
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
     * @param user      현재 로그인 유저
     */
    @MessageMapping("/story/{sessionId}")
    public void processTurn(@DestinationVariable Long sessionId,
                            @Valid @Payload StoryInputMessage message,
                            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        String destination = "/topic/story/" + sessionId;

        if (isForbidden(sessionId, user, destination)) {
            return;
        }

        // 자유 입력일 때만 모더레이션 체크 (선택지는 정해진 문구라 대상 아님)
        boolean isFreeText = message.choiceIndex() == null && message.content() != null && !message.content().isBlank();
        if (isFreeText && chatModerationService.isBlocked(sessionId, user.getUser().getId(), message.content())) {
            messagingTemplate.convertAndSend(destination,
                    new StoryMessageResponse(
                            "ERROR",
                            null,
                            "부적절한 입력이 감지되어 진행할 수 없습니다.",
                            null)
            );
            return;
        }

        log.info("스토리 턴 요청 sessionId: {} type: {}", sessionId, message.type());

        storyService.processStoryStream(
                sessionId,
                message.content(),
                message.choiceIndex(),
                chunk -> messagingTemplate.convertAndSend(
                        destination, new StoryMessageResponse("DELTA", "story", chunk, null)),
                result -> {
                    // 엔딩 도달 레디스에 본문 저장
                    if (result.getEndingType() != null) {
                        storyCacheService.saveEndingStory(sessionId, result.getStory());

                        // 유효 턴 완료 레디스에 상태 저장
                    } else if (result.getChoices() != null && !result.getChoices().isEmpty()) {
                        storyCacheService.savePlayState(
                                sessionId,
                                PlayStateCacheDTO.builder()
                                        .currentStory(result.getStory())
                                        .userInput(message.content())
                                        .choices(result.getChoices())
                                        .progressPercent(result.getProgressPercent())
                                        .build()
                        );
                    }
                    messagingTemplate.convertAndSend(
                            destination, new StoryMessageResponse("COMPLETE", null, null, result));
                },
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
     * @param user      현재 로그인 유저
     */
    @MessageMapping("/epilogue/{sessionId}")
    public void processEpilogue(@DestinationVariable Long sessionId,
                                @Valid @Payload EpilogueInputMessage message,
                                @AuthenticationPrincipal AuthenticatedUser user
    ) {
        String destination = "/topic/epilogue/" + sessionId;

        if (isForbidden(sessionId, user, destination)) {
            return;
        }

        // 에필로그는 항상 자유 입력이라 조건 없이 바로 모더레이션 체크
        if (chatModerationService.isBlocked(sessionId, user.getUser().getId(), message.content())) {
            messagingTemplate.convertAndSend(destination,
                    new StoryMessageResponse(
                            "ERROR",
                            null,
                            "부적절한 입력이 감지되어 진행할 수 없습니다.",
                            null)
            );
            return;
        }

        log.info("에필로그 턴 요청 sessionId: {} content: {}", sessionId, message.content());

        storyService.processEpilogueStream(
                sessionId,
                message.content(),
                chunk -> messagingTemplate.convertAndSend(
                        destination, new StoryMessageResponse("DELTA", "story", chunk, null)),
                () -> messagingTemplate.convertAndSend(
                        destination, new StoryMessageResponse("COMPLETE", null, null, null)),
                e -> {
                    log.error("에필로그 처리 실패 sessionId: {}", sessionId, e);
                    messagingTemplate.convertAndSend(
                            destination, new StoryMessageResponse("ERROR", null, e.getMessage(), null));
                }
        );
    }

    /* Helper Methode */

    /**
     * 세션 소유자 검증, 아니면 STOMP로 에러 응답 전송
     *
     * @param sessionId   세션 PK
     * @param user        현재 로그인 유저
     * @param destination STOMP 응답 destination
     * @return 소유자면 true, 아니면 false (에러 응답은 이 메서드 안에서 이미 전송됨)
     */
    private boolean isForbidden(Long sessionId, AuthenticatedUser user, String destination) {
        if (user == null || !storySessionsService.isOwner(sessionId, user.getUser().getId())) {
            messagingTemplate.convertAndSend(destination,
                    new StoryMessageResponse("ERROR", null, "권한이 없습니다.", null));
            return true;
        }
        return false;
    }
}

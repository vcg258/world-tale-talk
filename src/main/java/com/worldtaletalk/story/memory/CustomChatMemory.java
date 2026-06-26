package com.worldtaletalk.story.memory;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import com.worldtaletalk.chat.domain.ChatRole;
import com.worldtaletalk.chat.repository.ChatMessagesRepository;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomChatMemory implements ChatMemory {
    private final ChatMessagesRepository chatMessagesRepository;
    private final StorySessionsRepository storySessionsRepository;

    private static final int WINDOW_SIZE = 10;

    /**
     * 커스텀 ChatMemory 추가
     *
     * @param conversationId 세션 PK (String)
     * @param messages 저장할 메시지 목록
     */
    @Override
    @Transactional
    public void add(@NonNull String conversationId, @NonNull List<Message> messages) {
        StorySessionsEntity storySession = storySessionsRepository.findById(Long.parseLong(conversationId))
                .orElseThrow(() -> new NoSuchElementException("해당 세션을 찾을 수 없음"));

        for (Message message : messages) {
            ChatRole role;
            if (message instanceof UserMessage) {
                role = ChatRole.USER;
            } else if (message instanceof AssistantMessage) {
                role = ChatRole.ASSISTANT;
            } else {
                continue;
            }

            ChatMessagesEntity chatMessage = ChatMessagesEntity.builder()
                    .storySession(storySession)
                    .turn(storySession.getCurrentTurn())
                    .role(role)
                    .content(message.getText())
                    .epilogue(storySession.getStatus() == SessionStatus.EPILOGUE) // true = 에필로그 / false = 메인 스토리
                    .build();

            ChatMessagesEntity save = chatMessagesRepository.save(chatMessage);
            log.info("ChatMemory 저장 : {}", save);
        }
    }

    /**
     * 커스텀 ChatMemory 조회
     *
     * @param conversationId 세션 PK (String)
     * @return 메시지 목록
     */
    @Override
    @Transactional(readOnly = true)
    public List<Message> get(@NonNull String conversationId) {
        Long sessionId = Long.parseLong(conversationId);
        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId)
                .orElseThrow(() -> new NoSuchElementException("해당 세션을 찾을 수 없음"));

        // 에필로그 여부 확인후 에필로그 메세지만 조회를 하던 아니면 메인 메세지만 조회
        // DESC + LIMIT N 조회 후 reverse → 시간 오름차순 유지 (Spring AI 어드바이저가 시간순 기대)
        Pageable pageable = PageRequest.of(0, WINDOW_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ChatMessagesEntity> chatMessages = new ArrayList<>(
                storySession.getStatus() == SessionStatus.EPILOGUE ?
                        chatMessagesRepository.findAllEpilogueBySessionId(pageable, sessionId) :
                        chatMessagesRepository.findAllSessionId(pageable, sessionId)
        );
        Collections.reverse(chatMessages); // SpringAi는 시간순으로 기대하기 때문에 리버스 적용

        return chatMessages.stream()
                .map(entity -> {
                    if (entity.getRole().equals(ChatRole.USER)) return (Message) new UserMessage(entity.getContent());
                    if (entity.getRole().equals(ChatRole.ASSISTANT)) return (Message) new AssistantMessage(entity.getContent());
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 커스텀 ChatMemory 삭제 (세션의 모든 메시지 삭제)
     *
     * @param conversationId 세션 PK (String)
     */
    @Override
    @Transactional
    public void clear(@NonNull String conversationId) {
        chatMessagesRepository.deleteAllByStorySession_Id(Long.parseLong(conversationId));
        log.info("세션 모든 메세지 삭제 : {}", conversationId);
    }
}

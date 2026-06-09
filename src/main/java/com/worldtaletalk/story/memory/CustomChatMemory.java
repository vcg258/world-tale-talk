package com.worldtaletalk.story.memory;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import com.worldtaletalk.chat.domain.ChatRole;
import com.worldtaletalk.chat.repository.ChatMessagesRepository;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class CustomChatMemory implements ChatMemory {
    private final ChatMessagesRepository chatMessagesRepository;
    private final StorySessionsRepository storySessionsRepository;

    /**
     * 커스텀 ChatMemory 추가
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
                    .turn(storySession.getCurrentTurn()) // TODO StoryService에서 실제 턴 수 주입 예정
                    .role(role)
                    .content(message.getText())
                    .epilogue(false) // 기본값: 메인 스토리
                    .build();

            ChatMessagesEntity save = chatMessagesRepository.save(chatMessage);
            log.info("ChatMemory 저장 : {}", save);
        }
    }

    /**
     * 커스텀 ChatMemory 조회 (is_epilogue = false 인 메인 스토리 메시지만)
     * @param conversationId 세션 PK (String)
     * @return 메인 스토리 메시지 목록
     */
    @Override
    public List<Message> get(@NonNull String conversationId) {
        List<ChatMessagesEntity> chatMessages = chatMessagesRepository
                .findAllSessionId(Long.parseLong(conversationId));

        return chatMessages.stream()
                .map(entity -> {
                    if (entity.getRole().equals(ChatRole.USER)) {
                        return (Message) new UserMessage(entity.getContent());
                    }
                    if (entity.getRole().equals(ChatRole.ASSISTANT)) {
                        return (Message) new AssistantMessage(entity.getContent());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 커스텀 ChatMemory 삭제 (세션의 모든 메시지 삭제)
     * @param conversationId 세션 PK (String)
     */
    @Override
    @Transactional
    public void clear(@NonNull String conversationId) {
        chatMessagesRepository.deleteAllByStorySession_Id(Long.parseLong(conversationId));
        log.info("세션 모든 메세지 삭제 : {}", conversationId);
    }
}

package com.worldtaletalk.chat.service;

import com.worldtaletalk.chat.ChatMessagesMapper;
import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import com.worldtaletalk.chat.domain.ChatRole;
import com.worldtaletalk.chat.dto.ChatMessagesDTO;
import com.worldtaletalk.chat.repository.ChatMessagesRepository;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.session.repository.StorySessionsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessagesServiceImpl implements ChatMessagesService {
    private final ChatMessagesRepository chatMessagesRepository;
    private final ChatMessagesMapper chatMessagesMapper;
    private final StorySessionsRepository storySessionsRepository;

    /**
     * 세션 채팅 내역 커서 기반 조회 (WebSocket 재연결시 이전 대화 복원용) (페이징)
     *
     * @param sessionId     세션 PK
     * @param lastCreatedAt 현재 화면 제일 위 메시지 createdAt (최초 요청시 null)
     * @return 채팅 메시지 목록 (최신순 20개)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ChatMessagesDTO> getChatMessages(Long sessionId, LocalDateTime lastCreatedAt) {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<ChatMessagesEntity> messages = lastCreatedAt == null ?
                chatMessagesRepository.findByStorySession_Id(pageable, sessionId) :
                chatMessagesRepository.findCursorBySessionId(pageable, sessionId, lastCreatedAt);
        return chatMessagesMapper.toDTOList(messages);
    }

    @Transactional
    @Override
    public void saveStoryExchange(Long sessionId, int turn, String userInput, String story) {
        if (story == null || story.isBlank()) {
            return;
        }

        StorySessionsEntity storySession = storySessionsRepository.findById(sessionId).orElseThrow(() ->
                new java.util.NoSuchElementException("해당 세션을 찾을 수 없습니다."));

        boolean shouldSaveUserInput = userInput != null
                && !userInput.isBlank()
                && !"이야기를 시작해주세요.".equals(userInput.trim());

        if (shouldSaveUserInput) {
            chatMessagesRepository.save(ChatMessagesEntity.builder()
                    .storySession(storySession)
                    .turn(turn)
                    .role(ChatRole.USER)
                    .content(userInput)
                    .epilogue(false)
                    .build());
        }

        chatMessagesRepository.save(ChatMessagesEntity.builder()
                .storySession(storySession)
                .turn(turn)
                .role(ChatRole.ASSISTANT)
                .content(story)
                .epilogue(false)
                .build());
    }
}

package com.worldtaletalk.chat.repository;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessagesRepository extends JpaRepository<ChatMessagesEntity, Long> {
    // 세션의 모든 메시지 조회 (메인 에필로그 모두 포함) createdAt 기준 오름차순 정렬
    @Query("""
    SELECT c FROM ChatMessagesEntity c
        WHERE c.storySession.id = :id
            ORDER BY c.createdAt ASC
    """)
    List<ChatMessagesEntity> findAllByStorySessionId(@Param("id") Long id);

    // 세션의 메인 스토리 메시지만 조회
    @Query("""
    SELECT c FROM ChatMessagesEntity c
        WHERE c.storySession.id = :id AND c.epilogue = FALSE
            ORDER BY c.createdAt ASC
    """)
    List<ChatMessagesEntity> findAllSessionId(@Param("id") Long id);

    // 세션의 에필로그 스토리 메시지만 조회
    @Query("""
    SELECT c FROM ChatMessagesEntity c
        WHERE c.storySession.id = :id AND c.epilogue = TRUE
            ORDER BY c.createdAt ASC
    """)
    List<ChatMessagesEntity> findAllEpilogueBySessionId(@Param("id") Long id);

    // 세션의 메시지 전체 삭제
    void deleteAllByStorySession_Id(Long sessionId);
}

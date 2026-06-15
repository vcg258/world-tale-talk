package com.worldtaletalk.chat.repository;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    // 세션의 에필로그 스토리 메시지만 조회 TODO 일단 보류 없앨지 고민 해봐야할듯
    @Query("""
    SELECT c FROM ChatMessagesEntity c
        WHERE c.storySession.id = :id AND c.epilogue = TRUE
            ORDER BY c.createdAt ASC
    """)
    List<ChatMessagesEntity> findAllEpilogueBySessionId(@Param("id") Long id);

    // 세션의 메시지 전체 삭제
    void deleteAllByStorySession_Id(Long sessionId);

    // 해당 세션 메시지 전체 최초 조회 (스크롤 형식 페이징)
    List<ChatMessagesEntity> findByStorySession_IdOrderByCreatedAtDesc(Pageable pageable, Long id);

    // 해당 세션 메시지 전체 조회 (스크롤 형식 페이징)
    @Query("SELECT c FROM ChatMessagesEntity c WHERE c.storySession.id = :sessionId AND c.createdAt < :lastCreateAt")
    List<ChatMessagesEntity> findCursorBySessionId(Pageable pageable, @Param("sessionId") Long id, @Param("lastCreateAt") LocalDateTime lastCreateAt);
}

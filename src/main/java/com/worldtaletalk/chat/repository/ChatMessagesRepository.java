package com.worldtaletalk.chat.repository;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessagesRepository extends JpaRepository<ChatMessagesEntity, Long> {
    // 세션의 메인 스토리 메시지만 조회 (is_epilogue = false)
    @Query("""
    SELECT c FROM ChatMessagesEntity c 
        WHERE c.storySession.id = :id AND c.epilogue = FALSE
            ORDER BY c.createdAt ASC
    """)
    List<ChatMessagesEntity> findAllSessionId(@Param("id") Long id);

    // 세션의 메시지 전체 삭제
    void deleteAllByStorySession_Id(Long sessionId);
}

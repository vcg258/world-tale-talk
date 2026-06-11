package com.worldtaletalk.stat.repository;

import com.worldtaletalk.stat.domain.SessionStatLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SessionStatLogsRepository extends JpaRepository<SessionStatLogsEntity, Long> {
    // 세션의 전체 스텟 로그 조회
    List<SessionStatLogsEntity> findAllByStorySession_Id(Long sessionId);
}

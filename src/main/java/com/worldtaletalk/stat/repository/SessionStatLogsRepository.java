package com.worldtaletalk.stat.repository;

import com.worldtaletalk.stat.domain.SessionStatLogsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionStatLogsRepository extends JpaRepository<SessionStatLogsEntity, Long> {
}

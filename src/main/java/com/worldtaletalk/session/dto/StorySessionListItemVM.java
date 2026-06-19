package com.worldtaletalk.session.dto;

import com.worldtaletalk.session.domain.SessionStatus;
import java.time.LocalDateTime;

public record StorySessionListItemVM(
        Long id,
        String worldName,
        String personaName,
        SessionStatus status,
        int currentTurn,
        LocalDateTime createdAt
) {}

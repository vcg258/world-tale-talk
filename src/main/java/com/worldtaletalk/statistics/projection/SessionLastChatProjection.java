package com.worldtaletalk.statistics.projection;

import java.time.LocalDateTime;

public interface SessionLastChatProjection {
    Long getSessionId();
    LocalDateTime getLastChatAt();
}

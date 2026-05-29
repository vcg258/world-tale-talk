package com.worldtaletalk.chat.repository;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessagesRepository extends JpaRepository<ChatMessagesEntity, Long> {
}

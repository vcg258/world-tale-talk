package com.worldtaletalk.session.repository;

import com.worldtaletalk.session.domain.StorySessionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StorySessionsRepository extends JpaRepository<StorySessionsEntity, Long> {
}

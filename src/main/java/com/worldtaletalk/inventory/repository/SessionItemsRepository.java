package com.worldtaletalk.inventory.repository;

import com.worldtaletalk.inventory.domain.SessionItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionItemsRepository extends JpaRepository<SessionItemsEntity, Long> {
}

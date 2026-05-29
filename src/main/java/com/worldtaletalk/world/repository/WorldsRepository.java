package com.worldtaletalk.world.repository;

import com.worldtaletalk.world.domain.WorldsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldsRepository extends JpaRepository<WorldsEntity, Long> {
}

package com.worldtaletalk.rating.repository;

import com.worldtaletalk.rating.domain.WorldRatingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorldRatingsRepository extends JpaRepository<WorldRatingsEntity, Long> {
}

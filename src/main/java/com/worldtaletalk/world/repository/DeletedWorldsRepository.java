package com.worldtaletalk.world.repository;

import com.worldtaletalk.world.domain.DeletedWorldsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeletedWorldsRepository extends JpaRepository<DeletedWorldsEntity, Long> {
}

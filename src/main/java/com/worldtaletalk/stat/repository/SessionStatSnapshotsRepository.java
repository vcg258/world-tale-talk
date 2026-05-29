package com.worldtaletalk.stat.repository;

import com.worldtaletalk.stat.domain.SessionStatSnapshotsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionStatSnapshotsRepository extends JpaRepository<SessionStatSnapshotsEntity, Long> {
}

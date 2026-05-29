package com.worldtaletalk.notice.repository;

import com.worldtaletalk.notice.domain.NoticesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticesRepository extends JpaRepository<NoticesEntity, Long> {
}

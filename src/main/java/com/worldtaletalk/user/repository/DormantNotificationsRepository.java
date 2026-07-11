package com.worldtaletalk.user.repository;

import com.worldtaletalk.user.domain.DormantNotificationsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DormantNotificationsRepository extends JpaRepository<DormantNotificationsEntity, Long> {
    // 휴면계정 메일 발송 내역 삭제
    void deleteByUserId(Long userId);
}

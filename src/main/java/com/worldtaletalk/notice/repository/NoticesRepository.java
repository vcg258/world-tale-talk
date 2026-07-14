package com.worldtaletalk.notice.repository;

import com.worldtaletalk.notice.domain.NoticesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticesRepository extends JpaRepository<NoticesEntity, Long> {
    // 활성화 되어 있는 공지글 조회 (페이징)
    Page<NoticesEntity> findAllByActiveTrue(Pageable pageable);
}

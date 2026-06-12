package com.worldtaletalk.inventory.repository;

import com.worldtaletalk.inventory.domain.SessionItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SessionItemsRepository extends JpaRepository<SessionItemsEntity, Long> {
    // 인벤토리에 존재유무가 TRUE 인 아이템만 조회
    @Query("SELECT s FROM SessionItemsEntity s WHERE s.storySession.id = :sessionId AND s.active = TRUE")
    List<SessionItemsEntity> findAllByItemActive(@Param("sessionId") long sessionId);

    // 인벤토리에 존재유무가 TRUE 인 아이템 이름으로 조회
    @Query("""
            SELECT s FROM SessionItemsEntity s
                        WHERE s.storySession.id = :sessionId AND s.itemName = :itemName AND s.active = TRUE
                                    ORDER BY s.createdAt LIMIT 1
            """)
    Optional<SessionItemsEntity> findAllByItemNameAndActiveTrue(@Param("sessionId") long sessionId, @Param("itemName") String itemName);
}

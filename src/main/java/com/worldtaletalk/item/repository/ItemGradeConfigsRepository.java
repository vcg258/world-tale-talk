package com.worldtaletalk.item.repository;

import com.worldtaletalk.item.domain.ItemGrade;
import com.worldtaletalk.item.domain.ItemGradeConfigsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemGradeConfigsRepository extends JpaRepository<ItemGradeConfigsEntity, Long> {
    // 아이템 등급으로 조회
    Optional<ItemGradeConfigsEntity> findByGrade(ItemGrade itemGrade);
}

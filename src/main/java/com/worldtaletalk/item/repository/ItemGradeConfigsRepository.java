package com.worldtaletalk.item.repository;

import com.worldtaletalk.item.domain.ItemGrade;
import com.worldtaletalk.item.domain.ItemGradeConfigsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemGradeConfigsRepository extends JpaRepository<ItemGradeConfigsEntity, Long> {
    // 아이템 등급으로 조회
    ItemGradeConfigsEntity findByGrade(ItemGrade itemGrade);
}

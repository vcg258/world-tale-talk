package com.worldtaletalk.item.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "item_grade_configs")
public class ItemGradeConfigsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 15)
    private ItemGrade grade;

    @Column(nullable = false)
    private int statSumMax; // 스텟 보정 최대치

    @Column(nullable = false)
    private int singleMax; // 단일 스텟 보정 최대치

    @Column(nullable = false)
    private int singleMin; // 단일 스텟 보정 최소치

    @Builder.Default
    @Column(nullable = false)
    private boolean canNegative = false; // 음수 보정 허용여부 (True = 음수 허용)
}

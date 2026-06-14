package com.worldtaletalk.item.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "item_grade_configs")
public class ItemGradeConfigsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 15)
    private ItemGrade grade; // 아이템 등급 (COMMON, RARE, EPIC, CURSED)

    @Column(nullable = false)
    private int statSumMax; // 스텟 보정 최대치

    @Column(nullable = false)
    private int singleMax; // 단일 스텟 보정 최대치

    @Column(nullable = false)
    private int singleMin; // 단일 스텟 보정 최소치

    @Builder.Default
    @Column(nullable = false)
    private boolean canNegative = false; // 음수 보정 허용여부 (True = 음수 허용)

    /* 도메인 행위 메서드 */

    /**
     * 아이템 등급 보정 설정 수정 (관리자)
     * @param statSumMax  스텟 보정 합산 최대치
     * @param singleMax   단일 스텟 보정 최대치
     * @param singleMin   단일 스텟 보정 최소치
     * @param canNegative 음수 보정 허용 여부
     */
    public void updateConfig(int statSumMax, int singleMax, int singleMin, boolean canNegative) {
        this.statSumMax = statSumMax;
        this.singleMax = singleMax;
        this.singleMin = singleMin;
        this.canNegative = canNegative;
    }
}

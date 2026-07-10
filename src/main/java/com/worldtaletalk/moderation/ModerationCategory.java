package com.worldtaletalk.moderation;

import org.springframework.ai.moderation.Categories;


public class ModerationCategory {

    /**
     * 하드블록 대상 카테고리인지 판정
     *
     * @param categories 모더레이션 응답 카테고리
     * @return 하드블록 대상이면 true
     */
    public static boolean isHardBlock(Categories categories) {
        return categories.isSexualMinors() // 청불 카테고리
                || categories.isSelfHarmInstructions() // 자살 / 자해 카테고리
                || categories.isHateThreatening() // 인종 / 성별 / 종교 차별 카테고리
                || categories.isHarassmentThreatening(); // 특정 대상 괴롭힘 카테고리
    }
}

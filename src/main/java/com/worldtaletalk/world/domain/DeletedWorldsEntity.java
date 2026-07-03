package com.worldtaletalk.world.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "deleted_worlds")
public class DeletedWorldsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(length = 50, nullable = false)
    private String name; // 세계관 이름

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private WorldGenre genre; // 세계관 장르

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String description; // 세계관 설명

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String settingText; // 시스템 프롬프트 (LLM 주입용)

    @Builder.Default
    @Column(nullable = false)
    private int targetTurn = 20; // 메인 스토리 목표 턴 수

    @Builder.Default
    @Column(precision = 3, scale = 2, nullable = false)
    private BigDecimal ratingAvg = BigDecimal.ZERO; // 평균 평점

    @Builder.Default
    @Column(nullable = false)
    private int completedCount = 0; // 완결한 세션 수

    @Builder.Default
    @Column(nullable = false)
    private int abandonedCount = 0; // 중도포기한 세션 수

    @Builder.Default
    @Column(nullable = false)
    private int playCount = 0; // 전체 플레이 수


    /* 도메인 행위 메서드 */

    /**
     * 삭제된 세계관 생성
     *
     * @param name 이름
     * @param genre 장르
     * @param description 설명
     * @param settingText LLM 프롬프트
     * @param targetTurn 엔딩 턴수
     * @param ratingAvg 평점
     * @param completedCount 완결낸 수
     * @param abandonedCount 중도포기한 수
     * @param playCount 플레이 수
     * @return 세계관 스냅샷
     */
    public static DeletedWorldsEntity createDelWorld(
            String name,
            WorldGenre genre,
            String description,
            String settingText,
            int targetTurn,
            BigDecimal ratingAvg,
            int completedCount,
            int abandonedCount,
            int playCount
    ) {

        return DeletedWorldsEntity.builder()
                .name(name)
                .genre(genre)
                .description(description)
                .settingText(settingText)
                .targetTurn(targetTurn)
                .ratingAvg(ratingAvg)
                .completedCount(completedCount)
                .abandonedCount(abandonedCount)
                .playCount(playCount)
                .build();
    }

}

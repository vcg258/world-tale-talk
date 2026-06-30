package com.worldtaletalk.world.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.persona.domain.PersonasEntity;
import com.worldtaletalk.rating.domain.WorldRatingsEntity;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "worlds", indexes = {
        @Index(name = "idx_worlds_is_active", columnList = "is_active"),
        @Index(name = "idx_worlds_rating_avg", columnList = "rating_avg")
})
public class WorldsEntity extends BaseTimeEntity {
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
    @Column(name = "is_active", nullable = false)
    private boolean active = false; // 메인 화면 활성화 여부 (false = 비공개)

    @Builder.Default
    @Column(precision = 3, scale = 2, nullable = false)
    private BigDecimal ratingAvg = BigDecimal.ZERO; // 평균 평점 (생성 시 0 고정)

    @Builder.Default
    @Column(nullable = false)
    private int playCount = 0; // 전체 플레이 수 (생성 시 0 고정)

    // 별점 및 리뷰가 동시적으로 요청이 들어올 경우를 생각해서 버전관리 명시용으로 적용 (나중에 요청이 들어온쪽을 JPA가 예외 발생을 시킴)
    @Version
    private Long version;

    /* FK (부모) */
    @ToString.Exclude
    @OneToMany(mappedBy = "world")
    private List<PersonasEntity> persona;

    @ToString.Exclude
    @OneToMany(mappedBy = "worlds")
    private List<StorySessionsEntity> storySessions;

    @ToString.Exclude
    @OneToMany(mappedBy = "world")
    private List<WorldRatingsEntity> worldRatings;

    /* 도메인 행위 메서드 */
    /**
     * 세계관 생성
     *
     * @param name 이름
     * @param genre 장르
     * @param description 설명
     * @param settingText LLM 프롬프트
     * @param targetTurn 엔딩 턴수
     * @param active 활성화 여부
     * @return 세계관
     */
    public static WorldsEntity createWorld(
            String name,
            WorldGenre genre,
            String description,
            String settingText,
            int targetTurn,
            boolean active
    ) {

        return WorldsEntity.builder()
                .name(name)
                .genre(genre)
                .description(description)
                .settingText(settingText)
                .targetTurn(targetTurn)
                .active(active)
                .build();
    }

    /**
     * 업데이트 도메인 메서드
     *
     * @param name 이름
     * @param genre 장르
     * @param description 설명
     * @param settingText LLM 프롬프트
     * @param targetTurn 엔딩 턴수
     * @param active 활성화 여부
     */
    public void modifyInformationByWorld(
            String name,
            WorldGenre genre,
            String description,
            String settingText,
            int targetTurn,
            boolean active
    ) {
        this.name = name;
        this.genre = genre;
        this.description = description;
        this.settingText = settingText;
        this.targetTurn = targetTurn;
        this.active = active;
    }

    /**
     * 해당 세계관 세션을 만들대 플레이 수 증가
     */
    public void playCountUp() {
        this.playCount++;
    }

    /**
     * 세계관 평균 평점 변경
     *
     * @param rating 평점
     */
    public void changeRating(BigDecimal rating) {
        this.ratingAvg = rating;
    }

}

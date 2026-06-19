package com.worldtaletalk.stat.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "session_stat_snapshots")
public class SessionStatSnapshotsEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(nullable = false)
    private int wisdom; // 엔딩 시점 지혜

    @Column(nullable = false)
    private int strength; // 엔딩 시점 힘

    @Column(nullable = false)
    private int agility; // 엔딩 시점 민첩

    @Column(nullable = false)
    private int charisma; // 엔딩 시점 카리스마

    @Column(nullable = false)
    private int luck; // 엔딩 시점 행운

    @Column(length = Integer.MAX_VALUE) // null 허용 이유 : LLM이 메인스토리 요약을 해주기때문에 트랜잭션 밖에 있어야함
    private String storySummary; // 엔딩 시점 메인 스토리 요약본

    /* FK (자식) */
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_session_stat_snapshots_session"))
    private StorySessionsEntity storySession;

    /* 도메인 행위 메서드 */

    /**
     * 스텟 스냅샷 생성
     *
     * @param wisdom 지혜
     * @param strength 힘
     * @param agility 민첩
     * @param charisma 카르스마
     * @param luck 행운
     * @param storySession 세션 FK
     * @return 스탯 스냅샷
     */
    public static SessionStatSnapshotsEntity createSessionStatSnapshots(
            int wisdom,
            int strength,
            int agility,
            int charisma,
            int luck,
            StorySessionsEntity storySession
    ) {
        return SessionStatSnapshotsEntity.builder()
                .wisdom(wisdom)
                .strength(strength)
                .agility(agility)
                .charisma(charisma)
                .luck(luck)
                .storySession(storySession)
                .build();
    }

    /**
     * 엔딩이후 메인 스토리 요약
     *
     * @param summary 메인스토리 요약 히스토리
     */
    public void modifySnapshotSummary(String summary) {
        this.storySummary = summary;
    }
}

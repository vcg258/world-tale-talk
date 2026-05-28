package com.worldtaletalk.session.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.session.domain.storysessions.StorySessionsEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    /* FK (자식) */
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_session_stat_snapshots_session"))
    private StorySessionsEntity storySession;
}

package com.worldtaletalk.persona.domain;

import com.worldtaletalk.global.basetime.BaseTimeEntity;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import com.worldtaletalk.world.domain.WorldsEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "personas", indexes = @Index(name = "idx_personas_world_id", columnList = "world_id"))
public class PersonasEntity extends BaseTimeEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(length = 50, nullable = false)
    private String name; // 페르소나 이름

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String description; // 페르소나 소개

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String background; // 페르소나 배경(LLM 주입용)

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true; // 활성화 여부 (True 활성화)

    /* FK (부모) */
    @OneToOne(mappedBy = "persona")
    private PersonaBaseStatsEntity personaBaseStats;

    @OneToMany(mappedBy = "personas")
    private List<StorySessionsEntity> storySessions;


    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false, foreignKey = @ForeignKey(name = "fk_personas_world"))
    private WorldsEntity world;
}

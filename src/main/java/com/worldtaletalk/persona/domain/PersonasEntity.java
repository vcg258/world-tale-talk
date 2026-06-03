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
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    @Column(name = "is_active", nullable = false)
    private boolean active = false; // 활성화 여부 (false = 비공개)

    /* FK (부모) */
    @ToString.Exclude
    @OneToOne(mappedBy = "persona")
    private PersonasBaseStatsEntity personaBaseStats;

    @ToString.Exclude
    @OneToMany(mappedBy = "personas")
    private List<StorySessionsEntity> storySessions;


    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "world_id", nullable = false, foreignKey = @ForeignKey(name = "fk_personas_world"))
    private WorldsEntity world;

    /* 도메인 행위 메서드 */
    public static PersonasEntity addPersona(
            WorldsEntity worldsEntity,
            String name,
            String description,
            String background,
            boolean active
    ) {

        return PersonasEntity.builder()
                .name(name)
                .description(description)
                .background(background)
                .active(active)
                .world(worldsEntity)
                .build();
    }
}

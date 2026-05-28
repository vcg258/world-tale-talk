package com.worldtaletalk.persona.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "persona_base_stats", indexes
        = @Index(name = "idx_persona_base_stats_persona_persona_id", columnList = "persona_id"))
public class PersonaBaseStatsEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Builder.Default
    @Column(nullable = false)
    private int wisdom = 5; // 지혜

    @Builder.Default
    @Column(nullable = false)
    private int strength = 5; // 힘

    @Builder.Default
    @Column(nullable = false)
    private int agility = 5; // 민첩

    @Builder.Default
    @Column(nullable = false)
    private int charisma = 5; // 카리스마

    @Builder.Default
    @Column(nullable = false)
    private int luck = 5; // 행운

    /* FK (자식) */
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "persona_id", nullable = false, foreignKey = @ForeignKey(name = "fk_persona_base_stats_persona"))
    private PersonasEntity persona;
}

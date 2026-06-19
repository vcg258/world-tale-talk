package com.worldtaletalk.persona.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "persona_base_stats")
public class PersonasBaseStatsEntity {
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

    /* 도메인 행위 메서드 */
    /**
     * 페르소나 기본 스텟
     *
     * @param wisdom 지혜
     * @param strength 힘
     * @param agility 민첩
     * @param charisma 카리스마
     * @param luck 행운
     * @param persona 페르소나 정보
     * @return 페르소나 스텟
     */
    public static PersonasBaseStatsEntity createPersonaBaseStats(
            int wisdom,
            int strength,
            int agility,
            int charisma,
            int luck,
            PersonasEntity persona
    ) {
        return PersonasBaseStatsEntity.builder()
                .wisdom(wisdom)
                .strength(strength)
                .agility(agility)
                .charisma(charisma)
                .luck(luck)
                .persona(persona)
                .build();
    }

    /**
     * 해당 페르소나 스텟 수정
     *
     * @param wisdom 지혜
     * @param strength 힘
     * @param agility 민첩
     * @param charisma 카리스마
     * @param luck 행운
     */
    public void modifyInformation(
            int wisdom,
            int strength,
            int agility,
            int charisma,
            int luck
    ) {
        this.wisdom = wisdom;
        this.strength = strength;
        this.agility = agility;
        this.charisma = charisma;
        this.luck = luck;
    }

}

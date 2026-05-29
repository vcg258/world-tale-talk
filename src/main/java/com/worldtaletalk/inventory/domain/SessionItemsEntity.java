package com.worldtaletalk.inventory.domain;

import com.worldtaletalk.item.domain.ItemGrade;
import com.worldtaletalk.session.domain.StorySessionsEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "session_items", indexes = {
        @Index(name = "idx_session_items_session_id", columnList = "session_id"),
        @Index(name = "idx_session_items_session_active", columnList = "session_id, is_active")
})
public class SessionItemsEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id private Long id;

    @Column(length = 50, nullable = false)
    private String itemName; // LLM생성 아이템 이름

    @Column(length = Integer.MAX_VALUE, nullable = false)
    private String itemDescription; // LLM생성 아이템 설명

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private ItemGrade grade; // 아이템 등급

    @Builder.Default
    @Column(nullable = false)
    private int wisdomBonus = 0; // 지혜 보정치

    @Builder.Default
    @Column(nullable = false)
    private int strengthBonus = 0; // 힘 보정치

    @Builder.Default
    @Column(nullable = false)
    private int agilityBonus = 0; // 민첩 보정치

    @Builder.Default
    @Column(nullable = false)
    private int charismaBonus = 0; // 카리스마 보정치

    @Builder.Default
    @Column(nullable = false)
    private int luckBonus = 0; // 행운 보정치

    @Builder.Default
    @Column(nullable = false)
    private boolean isActive = true; // 아이템 보유여부 (true 보유)

    @Column(nullable = false)
    private Integer gainedAtTurn; // 아이템 획득한 턴

    private Integer droppedAtTurn; // 버린 턴 (null 이면 보유 중)

    /* FK (자식) */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, foreignKey = @ForeignKey(name = "fk_session_items_session"))
    private StorySessionsEntity storySession;
}

package com.worldtaletalk.inventory.dto;

import com.worldtaletalk.item.domain.ItemGrade;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionItemsDTO {
    private Long id;
    private Long storySessionId; // 플레이 세션 FK
    private String itemName; // LLM생성 아이템 이름
    private String itemDescription; // LLM생성 아이템 설명
    private ItemGrade grade; // 아이템 등급
    private int wisdomBonus; // 지혜 보정치
    private int strengthBonus; // 힘 보정치
    private int agilityBonus; // 민첩 보정치
    private int charismaBonus; // 카리스마 보정치
    private int luckBonus; // 행운 보정치
    private boolean active; // 아이템 보유여부 (true 보유)
    private Integer gainedAtTurn; // 아이템 흭득한 턴
    private Integer droppedAtTurn; // 버린 턴
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 보유 아이템 전체 스탯 보너스 합산
     * @param inventory 보유 중인 아이템 목록
     * @return 스탯별 보너스 합산 (wisdom, strength, agility, charisma, luck)
     */
    public static Map<String, Integer> sumBonuses(List<SessionItemsDTO> inventory) {
        return Map.of(
                "wisdom",   inventory.stream().mapToInt(SessionItemsDTO::getWisdomBonus).sum(),
                "strength", inventory.stream().mapToInt(SessionItemsDTO::getStrengthBonus).sum(),
                "agility",  inventory.stream().mapToInt(SessionItemsDTO::getAgilityBonus).sum(),
                "charisma", inventory.stream().mapToInt(SessionItemsDTO::getCharismaBonus).sum(),
                "luck",     inventory.stream().mapToInt(SessionItemsDTO::getLuckBonus).sum()
        );
    }
}

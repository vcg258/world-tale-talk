package com.worldtaletalk.inventory.dto;

import com.worldtaletalk.item.domain.ItemGrade;
import lombok.*;

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
}

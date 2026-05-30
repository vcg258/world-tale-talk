package com.worldtaletalk.rating.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldRatingsDTO {
    private Long id;
    private Long userId; // 유저 FK
    private Long worldId; // 세계관 FK
    private Long storySessionId; // 세션 FK
    private short score; // 별점 (1 ~ 5점)
    private String review; // 한줄 리뷰
}

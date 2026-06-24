package com.worldtaletalk.rating.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldRatingsDTO {
    private Long id;
    private Long userId; // 유저 FK
    private Long worldId; // 세계관 FK
    private Long storySessionId; // 세션 FK
    private Short score; // 별점 (1 ~ 5점)
    private String review; // 한줄 리뷰
    private Integer countPlayNumber; // 해당 세계관 클리어 횟수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String userNickname; // 해당 세션의 유저 닉네임 (FETCH JOIN 으로 닉네임을 가져오기 위함)
    private String worldName; // 해당 세션의 세계관 이름 (FETCH JOIN 으로 제목을 가져오기 위함)

}

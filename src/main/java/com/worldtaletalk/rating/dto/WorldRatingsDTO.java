package com.worldtaletalk.rating.dto;

import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.user.dto.UsersDTO;
import com.worldtaletalk.world.dto.WorldsDTO;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldRatingsDTO {
    private Long id;
    private UsersDTO user; // 유저 FK
    private WorldsDTO world; // 세계관 FK
    private StorySessionsDTO storySession; // 세션 FK
    private short score; // 별점 (1 ~ 5점)
    private String review; // 한줄 리뷰
}

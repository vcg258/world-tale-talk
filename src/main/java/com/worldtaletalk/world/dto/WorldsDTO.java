package com.worldtaletalk.world.dto;

import com.worldtaletalk.world.domain.WorldGenre;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorldsDTO {
    private Long id;
    private String name; // 세계관 이름
    private WorldGenre genre; // 세계관 장르
    private String description; // 세계관 설명
    private String settingText; // 시스템 프롬프트 (LLM 주입용)
    private Integer targetTurn; // 메인 스토리 목표 턴 수
    private Boolean active; // 메인 화면 활성화 여부 (false 유저에게 보여주지 않음)
    private BigDecimal ratingAvg; // 평균 평점
    private Integer playCount; // 전체 플레이 수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

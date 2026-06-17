package com.worldtaletalk.notice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticesDTO {
    private Long id;
    private Long adminId; // 관리자 FK
    private String title; // 공지 제목
    private String content; // 공지 본문
    private Boolean active; // 공지 공개 / 비공개 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

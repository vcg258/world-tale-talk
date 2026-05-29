package com.worldtaletalk.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSuspensionsDTO {
    private Long id;
    private UsersDTO users; // 회원 FK
    private UsersDTO admins; // 관리자 FK
    private String reason; // 정지 사유
    private LocalDateTime suspendedAt; // 정지 처리 일시
    private LocalDateTime liftedAt; // 정지 해제 일시 (NULL 이면 정지 중)

}

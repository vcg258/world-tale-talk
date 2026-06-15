package com.worldtaletalk.user.dto;

import com.worldtaletalk.user.domain.socialaccounts.SocialProvider;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialAccountsDTO {
    private Long id;
    private Long userId; // 회원 PK
    private SocialProvider provider; // 소셜 로그인 제공사
    private String providerId; // 소셜 제공사 고유번호
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

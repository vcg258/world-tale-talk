package com.worldtaletalk.user.dto;

import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.domain.user.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id; // PK
    private String loginId; // 로그인 아이디
    private String nickname; // 닉네임
    private String email; // 이메일
    private String password; // 비밀번호
    private UserRole role; // 유저 권한 (USER, ADMIN)
    private UserStatus status; // 계정 상태
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

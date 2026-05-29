package com.worldtaletalk.user.dto;

import com.worldtaletalk.user.domain.users.UserRole;
import com.worldtaletalk.user.domain.users.UserStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsersDTO {
    private Long id; // PK
    private String nickname; // 닉네임
    private String email; // 이메일
    private String password; // 비밀번호
    private UserRole role; // 유저 권한 (USER, ADMIN)
    private UserStatus status; // 계정 상태
}

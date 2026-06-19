package com.worldtaletalk.global.security;

import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails, AuthenticatedUser {
    private final UserDTO userDTO;

    @Override
    public Long getId() {
        return userDTO.getId();
    }

    /**
     * 권한 반환
     *
     * @return ROLE_USER 또는 ROLE_ADMIN
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + userDTO.getRole().name()));
    }

    /**
     * 비밀번호 반환
     *
     * @return 암호화된 비밀번호
     */
    @Override
    public String getPassword() {
        return userDTO.getPassword();
    }

    /**
     * 유저 이메일 반환
     *
     * @return 이메일
     */
    @Override
    public String getUsername() {
        return userDTO.getEmail();
    }

    /**
     * 계정 활성화 여부
     *
     * @return ACTIVE 상태일 때 true
     */
    @Override
    public boolean isEnabled() {
        return userDTO.getStatus().equals(UserStatus.ACTIVE);
    }
}

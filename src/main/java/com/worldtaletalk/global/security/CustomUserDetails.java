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

    /**
     * 유저 정보 반환
     *
     * @return 유저 정보
     */
    @Override
    public UserDTO getUser() {
        return userDTO;
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
     * 유저 로그인 아이디 반환
     *
     * @return 로그인 아이디
     */
    @Override
    public String getUsername() {
        return userDTO.getLoginId();
    }

    /**
     * 계정 활성화 여부
     *
     * @return ACTIVE 또는 DORMANT 상태일 때 true
     */
    @Override
    public boolean isEnabled() {
        UserStatus userStatus = userDTO.getStatus();
        return userStatus == UserStatus.ACTIVE || userStatus == UserStatus.DORMANT;
    }
}

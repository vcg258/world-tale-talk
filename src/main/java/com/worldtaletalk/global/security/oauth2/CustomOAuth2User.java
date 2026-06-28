package com.worldtaletalk.global.security.oauth2;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.user.dto.UserDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User, AuthenticatedUser {
    private final UserDTO userDTO; // 회원 정보
    private final Map<String, Object> attributes; // 회원 속성
    private final boolean isNewUser; // 최초 소셜로그인시 닉네임 변경 여부 (소셜로그인으로 들어올시 무조건적으로 닉네임을 받기 때문에 닉네임을 설정하도록 강제하기 위함)

    /**
     * 유저 식별자 반환 (이메일)
     *
     * @return 이메일
     */
    @Override
    public String getName() {
        return userDTO.getEmail();
    }

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
     * OAuth2 속성 반환
     *
     * @return 소셜 로그인 응답 속성
     */
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
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
}

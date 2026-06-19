package com.worldtaletalk.global.security.oauth2;

import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserService userService;

    /**
     * OAuth2 로그인 처리 (Google / Naver / Kakao)
     *
     * @param userRequest OAuth2 유저 요청
     * @return CustomOAuth2User
     */
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 소셜 제공자별 이메일 검증
        String email = extractEmail(registrationId, attributes);

        // 기존 회원인지 확인 조회
        UserDTO existsUser = userService.getUserByEmail(email);

        if (existsUser == null) {
            // 임시로 닉네임 지정 (소셜로그인으로 인해 소셜에 사용하던 닉네임이 중복될 수 있으므로 강제 지정)
            String tempNickname = "TEMP_" + UUID.randomUUID().toString().substring(0, 8);
            UserDTO newUser = UserDTO.builder()
                    .nickname(tempNickname)
                    .email(email)
                    .build();
            userService.createUser(newUser);
            UserDTO savedUser = userService.getUserByEmail(email);
            // 새로운 유저 true (임시닉네임이 부여받았기 때문에 강제로 닉네임 변경 시켜줄 예정)
            return new CustomOAuth2User(savedUser, attributes, true);
        }
        return new CustomOAuth2User(existsUser, attributes, false);
    }

    /**
     * 소셜 제공자별 이메일 추출
     *
     * @param registrationId 소셜 제공자 (google / naver / kakao)
     * @param attributes     OAuth2 속성
     * @return 이메일
     */
    private String extractEmail(String registrationId, Map<String, Object> attributes) {
        return (String) switch (registrationId) {
            case "google" -> attributes.get("email");
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield response.get("email");
            }
            case "kakao" -> {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                yield kakaoAccount.get("email");
            }
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }
}

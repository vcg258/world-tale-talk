package com.worldtaletalk.global.security.oauth2;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.user.domain.socialaccounts.SocialProvider;
import com.worldtaletalk.user.dto.SocialAccountsDTO;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.SocialAccountsService;
import com.worldtaletalk.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SocialAccountsService socialAccountsService;

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

        // 소셜 제공사 고유 번호
        String providerId = extractProviderId(registrationId, attributes);

        // 소셜 제공사
        SocialProvider provider = SocialProvider.valueOf(registrationId.toUpperCase());

        // 현재 로그인이 됐는지 확인
        Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
        if (currentAuth != null && currentAuth.isAuthenticated() &&
                currentAuth.getPrincipal() instanceof AuthenticatedUser currentUser) {
            // 기존 연동된 계정 여부 확인
            SocialAccountsDTO existingLink = socialAccountsService.getSocialAccount(provider, providerId);
            if (existingLink != null) {
                if(existingLink.getUserId().equals(currentUser.getUser().getId())) {
                    throw new OAuth2AuthenticationException("이미 연동된 계정입니다.");
                }
                throw new OAuth2AuthenticationException("이미 다른계정과 연동된 계정입니다.");
            }
            UserDTO linkUser = currentUser.getUser();
            SocialAccountsDTO newSocialAccount = SocialAccountsDTO.builder()
                    .userId(linkUser.getId())
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            socialAccountsService.createSocialAccount(newSocialAccount);
            return new CustomOAuth2User(linkUser, newSocialAccount, attributes, true);

        }

        // 이미 소셜 연동된 계정인지 확인
        SocialAccountsDTO existingLink = socialAccountsService.getSocialAccount(provider, providerId);
        if (existingLink != null) {
            UserDTO user = userService.getUser(existingLink.getUserId());
            return new CustomOAuth2User(user, existingLink, attributes, false);
        }

        // 기존 회원인지 확인 조회 (소셜계정 연동)
        UserDTO existsUser = userService.getUserByEmail(email);
        if (existsUser == null) {
            // 임시로 닉네임 지정 (소셜로그인으로 인해 소셜에 사용하던 닉네임이 중복될 수 있으므로 강제 지정)
            String tempNickname = "TEMP_" + UUID.randomUUID().toString().substring(0, 8);
            UserDTO newUser = UserDTO.builder()
                    .nickname(tempNickname)
                    .email(email)
                    .build();
            UserDTO user = userService.createUser(newUser);

            // 소셜계정 등록 (신규)
            SocialAccountsDTO newSocialAccounts = SocialAccountsDTO.builder()
                    .userId(user.getId())
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            socialAccountsService.createSocialAccount(newSocialAccounts);
            // 새로운 유저 true (임시닉네임이 부여받았기 때문에 강제로 닉네임 변경 시켜주도록 함)
            return new CustomOAuth2User(user, newSocialAccounts, attributes, true);
        }
        log.warn("이미 가입된 이메일입니다. 기존 방식으로 로그인후 회원정보에서 연동후 다시시도 하세요.");
        throw new OAuth2AuthenticationException("이미 가입된 이메일입니다. 기존 방식으로 로그인후 회원정보에서 연동후 다시시도 하세요.");
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

    /**
     * 소셜로그인 제공사 추출
     *
     * @param registrationId 소셜 제공사
     * @param attributes     속성 Map
     * @return 소셜 제공사 고유 ID
     */
    private String extractProviderId(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> (String) attributes.get("sub");
            case "naver" -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                yield (String) response.get("id");
            }
            case "kakao" -> String.valueOf(attributes.get("id")); // Long → String
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인: " + registrationId);
        };
    }
}

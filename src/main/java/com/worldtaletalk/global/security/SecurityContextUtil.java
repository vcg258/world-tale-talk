package com.worldtaletalk.global.security;

import com.worldtaletalk.global.security.oauth2.CustomOAuth2User;
import com.worldtaletalk.user.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.experimental.UtilityClass;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

@UtilityClass
public class SecurityContextUtil {

    /**
     * 닉네임 변경 후 SecurityContext 갱신
     * 로그아웃/재로그인 없이 세션에 즉시 반영
     *
     * @param user       현재 로그인 유저 (일반 / 소셜 공통)
     * @param updateUser 변경된 유저 정보
     * @param session    현재 HTTP 세션
     */
    public static void refresh(AuthenticatedUser user, UserDTO updateUser, HttpSession session) {
        Authentication newAuth;
        if (user instanceof CustomUserDetails) {
            newAuth = new UsernamePasswordAuthenticationToken(
                    new CustomUserDetails(updateUser),
                    null,
                    user.getAuthorities()
            );
        } else {
            OAuth2AuthenticationToken currentToken = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            newAuth = new OAuth2AuthenticationToken(
                    new CustomOAuth2User(updateUser, ((CustomOAuth2User) user).getSocialAccountsDTO(), ((CustomOAuth2User) user).getAttributes(), false),
                    user.getAuthorities(),
                    currentToken.getAuthorizedClientRegistrationId() // 소셜 제공자 식별자
            );
        }
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
    }

    /**
     * 최초 일반 자동 로그인시 SecurityContext 최초 생성
     *
     * @param userDetails 회원 정보
     * @param request 인증 / 인가 를 심기전에 세션 아이디를 변경하귀함 요청
     */
    public static void login(CustomUserDetails userDetails, HttpServletRequest request) {
        request.getSession(true);
        request.changeSessionId();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
    }
}

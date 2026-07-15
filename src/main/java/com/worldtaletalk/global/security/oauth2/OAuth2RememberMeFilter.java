package com.worldtaletalk.global.security.oauth2;

import com.worldtaletalk.global.security.RememberMeTokenRepository;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.SocialAccountsDTO;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.SocialAccountsService;
import com.worldtaletalk.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2RememberMeFilter extends OncePerRequestFilter {
    private final RememberMeTokenRepository rememberMeTokenRepository;
    private final UserService userService;
    private final SocialAccountsService socialAccountsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 이미 인증된 경우 스킵
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 최초 소셜 로그인 요청 시 rememberMe 파라미터를 세션에 저장 (쿠키 체크 전에 처리해야 신규 로그인에서도 저장됨)
        if (request.getRequestURI().startsWith("/oauth2/authorization")) {
            String rememberMe = request.getParameter("rememberMe");
            if ("true".equals(rememberMe)) {
                request.getSession().setAttribute("oauth2RememberMe", true);
            }
        }

        String cookieValue = extractCookie(request, "oauth2-remember-me");
        // 쿠키 값 확인 없으면 스킵
        if (cookieValue == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 이미 발급된 쿠키 디코딩후 식별자와 쿠키값으로 나눔
            String decoded = new String(Base64.getDecoder().decode(cookieValue));
            String[] parts = decoded.split(":");

            // series, token으로 나눠져있지않으면 만료 처리
            if (parts.length != 2) {
                invalidateCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            String series = parts[0];
            String token = parts[1];

            // 레디스 조회
            PersistentRememberMeToken stored = rememberMeTokenRepository.getTokenForSeries(series);

            if (stored == null) {
                invalidateCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            if (!stored.getTokenValue().equals(token)) {
                log.warn("OAuth2 토큰값 불일치 탈취 감지 series : {}", series);
                invalidateCookie(response);
                rememberMeTokenRepository.removeUserTokens(stored.getUsername());
                filterChain.doFilter(request, response);
                return;
            }

            // 조회한 유저가 없거나 정지, 탈퇴된 상태라면 만료처리
            UserDTO user = userService.getUser(RememberMeTokenRepository.decodeOAuth2UserId(stored.getUsername()));
            if (user == null || !UserStatus.ACTIVE.equals(user.getStatus())) {
                invalidateCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            // 기존 소셜회원 확인
            List<SocialAccountsDTO> socialAccounts = socialAccountsService.getSocialAccountsByUserId(user.getId());
            if (socialAccounts.isEmpty()) {
                invalidateCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            String newToken = UUID.randomUUID().toString();

            String rememberedProvider = rememberMeTokenRepository.getProvider(series);
            SocialAccountsDTO socialAccountsDTO = socialAccounts.stream()
                    .filter(s -> s.getProvider().name().equals(rememberedProvider))
                    .findFirst()
                    .orElseGet(() -> {
                        log.warn("리멤버미 기록된 provider({})가 더 이상 연동되어 있지 않음, userId : {}", rememberedProvider, user.getId());
                        SocialAccountsDTO fallback = socialAccounts.get(0);
                        rememberMeTokenRepository.attachProvider(series, fallback.getProvider().name());
                        return fallback;
                    });
            String registrationId = socialAccountsDTO.getProvider().name().toLowerCase();

            rememberMeTokenRepository.updateToken(series, newToken, new Date());

            String newCookieValue = Base64.getEncoder().encodeToString((series + ":" + newToken).getBytes());

            ResponseCookie newCookie = ResponseCookie.from("oauth2-remember-me", newCookieValue)
                    .httpOnly(true)
                    .maxAge(Duration.ofDays(30))
                    .path("/")
                    .sameSite("Strict")
                    .secure(false)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, newCookie.toString());

            CustomOAuth2User oAuth2User = new CustomOAuth2User(user, socialAccountsDTO, Map.of(), false);
            OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                    oAuth2User, oAuth2User.getAuthorities(), registrationId);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
            );

            log.info("OAuth2 자동로그인 성공 : email : {}, registrationId : {}", user.getEmail(), registrationId);

        } catch (Exception e) {
            log.warn("OAuth2 자동로그인 실패 : {}", e.getMessage());
            invalidateCookie(response);
        }

        filterChain.doFilter(request, response);
    }


    /* Helper Methode */

    /**
     * 해당 이름 쿠키 유무 확인
     *
     * @param request 요청
     * @param name    쿠키 이름
     * @return 해당 이름 쿠키 값
     */
    private String extractCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) return cookie.getValue();
        }
        return null;
    }

    /**
     * 소셜 자동 로그인 쿠키 만료 처리
     *
     * @param response 응답
     */
    private void invalidateCookie(HttpServletResponse response) {
        ResponseCookie expired = ResponseCookie.from("oauth2-remember-me", "")
                .httpOnly(true)
                .maxAge(0)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expired.toString());
    }
}

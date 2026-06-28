package com.worldtaletalk.global.security.oauth2;

import com.worldtaletalk.global.security.RememberMeTokenRepository;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
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
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2RememberMeRequestFilter extends OncePerRequestFilter {
    private final RememberMeTokenRepository rememberMeTokenRepository;
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 이미 인증된 경우 스킵
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
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
            if (parts.length != 2) {
                invalidateCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            String series = parts[0];
            String token = parts[1];

            PersistentRememberMeToken stored = rememberMeTokenRepository.getTokenForSeries(series);

            if (stored == null || !stored.getUsername().equals(token)) {
                log.warn("OAuth2 토큰값 불일치 탈취 감지 series : {}", series);
                invalidateCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            UserDTO user = userService.getUserByEmail(stored.getUsername());
            if (user == null || !UserStatus.ACTIVE.equals(user.getStatus())) {
                invalidateCookie(response);
                filterChain.doFilter(request, response);
                return;
            }

            String newToken = UUID.randomUUID().toString();
            rememberMeTokenRepository.updateToken(series, newToken, new Date());

            String newCookieValue = Base64.getEncoder().encodeToString((series + ":" + newToken).getBytes());

            ResponseCookie newCookie = ResponseCookie.from("oauth2-remember-me", newCookieValue)
                    .httpOnly(true)
                    .maxAge(Duration.ofDays(30))
                    .path("/")
                    .sameSite("Strict")
                    .secure(false) // TODO HTTPS 로 변경시 수정
                    .build();

            response.setHeader(HttpHeaders.SET_COOKIE, newCookie.toString());

            CustomOAuth2User oAuth2User = new CustomOAuth2User(user, Map.of(), false);
            OAuth2AuthenticationToken authToken = new OAuth2AuthenticationToken(
                    oAuth2User, oAuth2User.getAuthorities(), "google"); // TODO 일단 하드코딩 구글 조만간 수정예정

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
            );

            log.info("OAuth2 자동로그인 성공 : {}", user.getEmail());

        } catch (Exception e) {
            log.warn("OAuth2 자동로그인 실패 : {}", e.getMessage());
            invalidateCookie(response);
        }

        if (request.getRequestURI().startsWith("/oauth2/authorization")) {
            String rememberMe = request.getParameter("rememberMe");
            if ("true".equals(rememberMe)) {
                request.getSession().setAttribute("oauth2RememberMe", true);
            }
        }
        filterChain.doFilter(request, response);
    }


    /* Helper Methode */

    /**
     * 해당 이름 쿠키 유무 확인
     *
     * @param request 요청
     * @param name 쿠키 이름
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
     *
     *
     * @param response
     */
    private void invalidateCookie(HttpServletResponse response) {
        ResponseCookie expired = ResponseCookie.from("oauth2-remember-me", "")
                .httpOnly(true)
                .maxAge(0)
                .path("/")
                .build();
        response.setHeader(HttpHeaders.SET_COOKIE, expired.toString());
    }
}

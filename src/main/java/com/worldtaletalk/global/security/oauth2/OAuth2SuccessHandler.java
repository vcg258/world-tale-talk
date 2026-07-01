package com.worldtaletalk.global.security.oauth2;

import com.worldtaletalk.global.security.RememberMeTokenRepository;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final RememberMeTokenRepository rememberMeTokenRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication
    ) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        UserDTO userDTO = oAuth2User.getUser();

        if (UserStatus.SUSPENDED.equals(userDTO.getStatus())) {
            log.info("정지된 유저 로그인 시도: email={}", userDTO.getEmail());
            response.sendRedirect("/auth/suspended");
            return;
        }

        Boolean rememberMe = (Boolean) request.getSession().getAttribute("oauth2RememberMe");
        if (Boolean.TRUE.equals(rememberMe)) {
            String series = UUID.randomUUID().toString();
            String token = UUID.randomUUID().toString();

            rememberMeTokenRepository.createNewToken(
                    new PersistentRememberMeToken(String.valueOf(userDTO.getId()), series, token, new Date()));

            String cookieValue = Base64.getEncoder().encodeToString((series + ":" + token).getBytes());

            ResponseCookie cookie = ResponseCookie.from("oauth2-remember-me", cookieValue)
                    .httpOnly(true)
                    .maxAge(Duration.ofDays(30))
                    .path("/")
                    .sameSite("Strict")
                    .secure(false) // TODO HTTP false (HTTPS 로 변경시 수정필요함 그럴일 없을거같긴한데)
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            request.getSession().removeAttribute("oauth2RememberMe");
            log.info("소셜 자동 로그인 토큰 발급 email : {}", userDTO.getEmail());
        }

        // TODO 이건 협의 사항 새로운 페이지를 만들어야할 수 도 있음
        if (oAuth2User.isNewUser()) {
            log.info("신규 유저 닉네임 설정 페이지 이동: email={}", userDTO.getEmail());
            response.sendRedirect("/auth/nickname");
            return;
        }

        log.info("기존 유저 로그인 성공: email={}", userDTO.getEmail());
        response.sendRedirect("/");
    }
}

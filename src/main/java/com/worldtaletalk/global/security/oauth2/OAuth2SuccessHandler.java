package com.worldtaletalk.global.security.oauth2;

import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        UserDTO userDTO = oAuth2User.getUserDTO();

        if (UserStatus.SUSPENDED.equals(userDTO.getStatus())) {
            log.info("정지된 유저 로그인 시도: email={}", userDTO.getEmail());
            response.sendRedirect("/auth/suspended");
            return;
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

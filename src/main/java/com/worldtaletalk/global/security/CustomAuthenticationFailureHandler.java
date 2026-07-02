package com.worldtaletalk.global.security;

import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    private final UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception
    ) throws IOException, ServletException {
        if (exception instanceof DisabledException) {
            String loginId = request.getParameter("loginId");
            UserDTO user = userService.getUserByLoginId(loginId);

            if (user != null && user.getStatus() == UserStatus.SUSPENDED) {
                response.sendRedirect("/auth/suspended?loginId=" + loginId);
                return;
            }
        }

        response.sendRedirect("/auth/login?error=true");
    }
}

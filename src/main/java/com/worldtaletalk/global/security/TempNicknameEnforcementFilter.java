package com.worldtaletalk.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TempNicknameEnforcementFilter extends OncePerRequestFilter {
    private static final String TEMP_NICKNAME_PREFIX = "TEMP_";
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/auth/nickname",
            "/auth/logout",
            "/css/", "/js/", "/images/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 권한 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {

            // 닉네임을 조회 했는데 TEMP_로 시작일경우 최초 소셜로그인 유저 이므로 닉네임 변경 강제플로우 해당함
            String nickname = authenticatedUser.getUser().getNickname();
            if (nickname != null && nickname.startsWith(TEMP_NICKNAME_PREFIX) && !isExcluded(request.getRequestURI())) {
                response.sendRedirect("/auth/nickname");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    /* Helper Methode */


    /**
     * 접근 가능한 지정 URI 여부 확인용
     *
     * @param uri URI
     * @return 여부
     */
    private boolean isExcluded(String uri) {
        for (String excluded : EXCLUDED_PATHS) {
            if (uri.startsWith(excluded)) {
                return true;
            }
        }
        return false;
    }
}

package com.worldtaletalk.global.security;

import com.worldtaletalk.user.domain.user.UserStatus;
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
public class DormantAccountRedirectFilter extends OncePerRequestFilter {
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/auth/dormant", "/auth/logout", "/css/", "/js/", "/images/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {

            if (authenticatedUser.getUser().getStatus() == UserStatus.DORMANT && !isExcluded(request.getRequestURI())) {
                response.sendRedirect("/auth/dormant");
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

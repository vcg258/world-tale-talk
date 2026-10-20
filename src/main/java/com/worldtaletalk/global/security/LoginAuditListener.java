package com.worldtaletalk.global.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 로그인 시도 로그 기록
 * LoginActivityListener 와 같은 이유로 이벤트리스너를 사용 (일반 / 소셜 / 자동 로그인 처리 로그를 한곳으로 모으기 위함)
 */
@Slf4j
@Component
public class LoginAuditListener {
    private static final int MAX_ID_LENGTH = 50;

    // 로그인 성공 (일반 / 소셜 / 자동 로그인)
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
            log.info("[LOGIN] 성공 method={} userId={} ip={}",
                    resolveMethod(authentication), authenticatedUser.getUser().getId(), resolveClientIp());
        }
    }

    // 로그인 실패 (일반 로그인의 아이디 / 비밀번호 불일치, 정지 계정 등)
    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        Authentication authentication = event.getAuthentication();
        log.warn("[LOGIN] 실패 method={} id={} reason={} ip={}",
                resolveMethod(authentication), sanitize(authentication.getName()),
                event.getException().getClass().getSimpleName(), resolveClientIp());
    }


    /* Helper Methode */

    /**
     * 로그인 방식 구분
     *
     * @param authentication 인증 정보
     * @return form / remember-me / social:제공사
     */
    private String resolveMethod(Authentication authentication) {
        if (authentication instanceof OAuth2LoginAuthenticationToken oauth2Token) {
            return "social:" + oauth2Token.getClientRegistration().getRegistrationId();
        }
        if (authentication instanceof RememberMeAuthenticationToken) {
            return "remember-me";
        }
        return "form";
    }

    /**
     * 접속자 IP 확인 (Nginx 뒤에서는 getRemoteAddr 가 프록시 주소이므로 Nginx 가 설정하는 X-Real-IP 를 우선 사용)
     *
     * @return IP 주소 (요청 정보를 알 수 없으면 unknown)
     */
    private String resolveClientIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            String realIp = request.getHeader("X-Real-IP");
            return (realIp != null && !realIp.isBlank()) ? sanitize(realIp) : request.getRemoteAddr();
        }
        return "unknown";
    }

    /**
     * 사용자가 입력한 값을 로그에 남기기 전에 줄바꿈 제거 및 길이 제한 (로그 위조 방지)
     *
     * @param value 입력값
     * @return 정리된 값
     */
    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        String cleaned = value.replaceAll("[\\r\\n]", "_");
        return cleaned.length() > MAX_ID_LENGTH ? cleaned.substring(0, MAX_ID_LENGTH) : cleaned;
    }
}

package com.worldtaletalk.config;

import com.worldtaletalk.global.security.*;
import com.worldtaletalk.global.security.oauth2.CustomOAuth2UserService;
import com.worldtaletalk.global.security.oauth2.OAuth2RememberMeFilter;
import com.worldtaletalk.global.security.oauth2.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailsService customUserDetailsService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final RememberMeTokenRepository rememberMeTokenRepository;
    private final OAuth2RememberMeFilter oAuth2RememberMeFilter;
    private final TempNicknameEnforcementFilter tempNicknameEnforcementFilter;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final DormantAccountRedirectFilter dormantAccountRedirectFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", // 메인 화면
                                "/worlds", "/worlds/*", // 세계관 탐색 및 상세
                                "/notices", "/notices/**", // 공지사항 목록 및 상세
                                "/auth/login", // 로그인
                                "/auth/nickname", "/auth/nickname/check", // 닉네임 설정 및 중복 검사
                                "/auth/suspended", // 정지 안내
                                "/auth/signup", "/auth/signup/**", // 회원가입
                                "/auth/find-id", // 아이디 찾기
                                "/auth/find-password", "/auth/reset-password", // 비밀번호 찾기
                                "/auth/dormant", // 휴면 안내
                                "/css/**", "/js/**", "/images/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN") // /admin 시작 관리자 권한 아니면 접근 불가
                        .anyRequest().authenticated() // 그 외 모든 요청 인증 필요
                )
                .sessionManagement(
                        session -> session
                                .sessionFixation(fix -> fix.changeSessionId())
                                .maximumSessions(-1)
                                .sessionRegistry(sessionRegistry())
                                .expiredUrl("/auth/login?expired=true")
                )
                .formLogin(auth -> auth
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/", false)
                        .failureHandler(customAuthenticationFailureHandler)
                        .usernameParameter("loginId")
                        .passwordParameter("password")

                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/auth/login?error=true")
                )
                .rememberMe(remember -> remember
                        .rememberMeParameter("rememberMe")
                        .tokenValiditySeconds(60 * 60 * 24 * 30)
                        .tokenRepository(rememberMeTokenRepository)
                        .userDetailsService(customUserDetailsService)
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, e) ->
                                response.sendRedirect("/auth/login"))
                        .accessDeniedHandler((request, response, e) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN))
                )
                .addFilterBefore(oAuth2RememberMeFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tempNicknameEnforcementFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(dormantAccountRedirectFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/ws/**") // 웹소켓 제외
                        .ignoringRequestMatchers("/auth/signup/send-code") // 회원가입 이메일 OTP 메일 발송 REST 제외
                        .ignoringRequestMatchers("/auth/signup/verify-code") // 회원가입 OTP 검증 REST 제외
                );
        return http.build();
    }

    /**
     * 세션 메니저(관리) 등록
     *
     * @return 세션 매니저 구현체
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    /**
     * 세션 만료이벤트를 추적하기 위함
     *
     * @return 세션 만료이벤트
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}

package com.worldtaletalk.config;

import com.worldtaletalk.global.security.oauth2.CustomOAuth2UserService;
import com.worldtaletalk.global.security.oauth2.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOAuth2UserService customOAuth2UserService, OAuth2SuccessHandler oAuth2SuccessHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                "/", // 메인 화면
//                                "/auth/login", // 로그인
//                                "/auth/nickname", // 닉네임 설정
//                                "/auth/suspended", // 정지 안내
//                                "/css/**", "/js/**", "/images/**"
//                        ).permitAll()
//                        .anyRequest().authenticated() // 그 외 모든 요청 인증 필요
                            .anyRequest().permitAll() // TODO 임시로 열어둠
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

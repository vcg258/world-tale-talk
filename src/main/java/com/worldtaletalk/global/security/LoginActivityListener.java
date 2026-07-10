package com.worldtaletalk.global.security;

import com.worldtaletalk.user.domain.user.UserEntity;
import com.worldtaletalk.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginActivityListener {
    private final UserRepository userRepository;

    /*
    회원 로그인시 마지막 로그인 일시 업데이트
    (이벤트리스너를 여기에만 사용한 이유 : 소셜로그인 일반로그인 두가지 방식의 처리를 한곳으로 모으는게 더 효율적이라 생각했기 때문)
    */
    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        if (event.getAuthentication().getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
            UserEntity user = userRepository.findById(authenticatedUser.getUser().getId()).orElseThrow(() ->
                    new NoSuchElementException("해당 회원은 존재하지 않습니다."));
            user.changeLastLoginAt();
        }
    }
}

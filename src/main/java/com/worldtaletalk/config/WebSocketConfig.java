package com.worldtaletalk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 지정 url로 시작하는 destination은 브로커가 구독자에게 직접 브로드캐스트
        registry.enableSimpleBroker("/topic", "/queue");
        // 클라이언트에서 서버 메시지는 /app 접두사가 붙은 MessageMapping으로 라우팅
        registry.setApplicationDestinationPrefixes("/app");
        // 특정 유저 전용 메시지 전송시 사용하는 접두사
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // Security의 Authenticate를 사용하기 위한 Resolver 설정
        argumentResolvers.add(new AuthenticationPrincipalArgumentResolver());
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // @MessageMapping을 사용할때 Principal의 헤더정보를 넘겨줄 Resolver 설정
        registration.interceptors(new SecurityContextChannelInterceptor());
    }
}

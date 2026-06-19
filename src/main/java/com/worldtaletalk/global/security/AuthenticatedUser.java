package com.worldtaletalk.global.security;

// 일반 로그인과 소셜로그인을 연동하는 작업에서 데이터타입이 달라지는 문제가 발생함 인터페이스로 PK를 받아줌으로써 해결
public interface AuthenticatedUser {
    Long getId();
}

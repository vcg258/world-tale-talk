package com.worldtaletalk.global.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 요청 예외
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage()); // 400
    }

    // 중복, 이미 처리됨 예외
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handlerIllegalStateException(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage()); // 400
    }

    // 매개변수와 비지니스로직은 정상 (해당값이 없음)
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handlerNoSuchElementException(NoSuchElementException e) {
        return ResponseEntity.status(404).body(e.getMessage()); // 404
    }

    // 그 외 서버에러
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handlerRuntimeException(RuntimeException e) {
        return ResponseEntity.internalServerError().body(e.getMessage()); // 500
    }
}

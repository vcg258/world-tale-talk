package com.worldtaletalk.global.handler;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    // 잘못된 요청 예외
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/400"; // 400
    }

    // 중복, 이미 처리됨 예외
    @ExceptionHandler(IllegalStateException.class)
    public String handlerIllegalStateException(IllegalStateException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/400"; // 400
    }

    // 매개변수와 비지니스로직은 정상 (해당값이 없음)
    @ExceptionHandler(NoSuchElementException.class)
    public String handlerNoSuchElementException(NoSuchElementException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/404"; // 404
    }

    // 그 외 서버에러
    @ExceptionHandler(RuntimeException.class)
    public String handlerRuntimeException(RuntimeException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error/500"; // 500
    }
}

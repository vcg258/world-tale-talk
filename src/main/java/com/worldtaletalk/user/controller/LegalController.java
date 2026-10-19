package com.worldtaletalk.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalController {

    /**
     * 이용약관 화면 조회
     *
     * @return legal/terms (이용약관 페이지)
     */
    @GetMapping("/terms")
    public String terms() {
        return "legal/terms";
    }

    /**
     * 개인정보처리방침 화면 조회
     *
     * @return legal/privacy (개인정보처리방침 페이지)
     */
    @GetMapping("/privacy")
    public String privacy() {
        return "legal/privacy";
    }
}

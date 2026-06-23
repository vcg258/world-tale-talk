package com.worldtaletalk.notice.controller;

import com.worldtaletalk.notice.service.NoticesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/notices")
@RequiredArgsConstructor
public class NoticeViewController {
    private final NoticesService noticesService;

    /**
     * N-01: 공지 목록 조회
     *
     * @param page  페이지 번호 (1부터 시작)
     * @param model 뷰에 전달할 모델
     * @return notice/list (공지 목록 페이지)
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("notices", noticesService.getActiveNotices(page));
        return "notice/list";
    }

    /**
     * N-02: 공지 상세 조회
     *
     * @param id    공지 PK
     * @param model 뷰에 전달할 모델
     * @return notice/detail (공지 상세 페이지)
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticesService.getNotice(id));
        return "notice/detail";
    }
}
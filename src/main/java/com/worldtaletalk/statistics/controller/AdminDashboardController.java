package com.worldtaletalk.statistics.controller;

import com.worldtaletalk.statistics.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminStatsService adminStatsService;

    /**
     * AD-01: 어드민 대시보드 메인
     *
     * @param model 뷰에 전달할 모델
     * @return admin/dashboard (어드민 대시보드 페이지)
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("stats", adminStatsService.getDashboardStats());
        return "admin/dashboard";
    }

    /**
     * AD-02: 세계관별 통계 조회
     *
     * @param model 뷰에 전달할 모델
     * @return admin/stats/index (세계관 통계 페이지)
     */
    @GetMapping("/stats")
    public String stats(Model model) {
        model.addAttribute("worldStats", adminStatsService.getWorldStats());
        return "admin/stats/index";
    }
}

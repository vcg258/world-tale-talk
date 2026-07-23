package com.worldtaletalk.statistics.controller;

import com.worldtaletalk.statistics.dto.AdminDetailedStatsDTO;
import com.worldtaletalk.statistics.dto.UsageStatsItemDTO;
import com.worldtaletalk.statistics.dto.TimeSeriesStatsDTO;
import com.worldtaletalk.statistics.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

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
    public String stats(
            @RequestParam(defaultValue = "today") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        LocalDate today = LocalDate.now();
        LocalDate resolvedStart = switch (period) {
            case "week" -> today.minusDays(6);
            case "month" -> today.minusDays(29);
            case "custom" -> startDate != null ? startDate : today;
            default -> today;
        };
        LocalDate resolvedEnd = "custom".equals(period) && endDate != null ? endDate : today;

        AdminDetailedStatsDTO detailedStats = adminStatsService.getDetailedStats(resolvedStart, resolvedEnd);
        model.addAttribute("period", period);
        model.addAttribute("startDate", detailedStats.getStartDate());
        model.addAttribute("endDate", detailedStats.getEndDate());
        model.addAttribute("worldStats", adminStatsService.getWorldStats());
        model.addAttribute("detailedStats", detailedStats);
        model.addAttribute("statusLabels", List.of("엔딩", "포기", "진행중"));
        model.addAttribute("statusCounts", List.of(
                detailedStats.getEndingCount(),
                detailedStats.getAbandonedCount(),
                detailedStats.getActiveCount()
        ));
        model.addAttribute("worldLabels", detailedStats.getWorldUsage().stream().map(UsageStatsItemDTO::getLabel).toList());
        model.addAttribute("worldCounts", detailedStats.getWorldUsage().stream().map(UsageStatsItemDTO::getCount).toList());
        model.addAttribute("personaLabels", detailedStats.getPersonaUsage().stream().map(UsageStatsItemDTO::getLabel).toList());
        model.addAttribute("personaCounts", detailedStats.getPersonaUsage().stream().map(UsageStatsItemDTO::getCount).toList());
        model.addAttribute("personaGroups", detailedStats.getPersonaUsage().stream().map(UsageStatsItemDTO::getGroupLabel).toList());
        model.addAttribute("timeLabels", detailedStats.getTimeSeries().stream().map(TimeSeriesStatsDTO::getDate).toList());
        model.addAttribute("timeTotalCounts", detailedStats.getTimeSeries().stream().map(TimeSeriesStatsDTO::getTotalStories).toList());
        model.addAttribute("timeEndingCounts", detailedStats.getTimeSeries().stream().map(TimeSeriesStatsDTO::getEndingCount).toList());
        model.addAttribute("timeAbandonedCounts", detailedStats.getTimeSeries().stream().map(TimeSeriesStatsDTO::getAbandonedCount).toList());
        model.addAttribute("timeActiveCounts", detailedStats.getTimeSeries().stream().map(TimeSeriesStatsDTO::getActiveCount).toList());
        return "admin/stats/index";
    }
}

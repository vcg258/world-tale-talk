package com.worldtaletalk.report.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.report.domain.ReportStatus;
import com.worldtaletalk.report.dto.ReportsDTO;
import com.worldtaletalk.report.service.ReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/report")
@RequiredArgsConstructor
public class AdminReportController {
    private final ReportsService reportsService;

    /**
     * ART - 01: 신고 목록 조회
     *
     * @param page  페이지 번호
     * @param model 뷰에 전달할 모델
     * @return admin/report/list (신고 목록 페이지)
     */
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) ReportStatus status,
            Model model
    ) {
        Page<ReportsDTO> reports = reportsService.getReports(status, page);
        model.addAttribute("reports", reports.getContent());
        model.addAttribute("reportPage", reports);
        model.addAttribute("status", status);
        return "admin/report/list";
    }

    /**
     * ART-02: 신고 처리
     *
     * @param id                 신고 PK
     * @param status             처리 상태
     * @param admin              현재 로그인한 관리자
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/report (신고 목록 페이지)
     */
    @PostMapping("/{id}/handle")
    public String handle(
            @PathVariable Long id,
            @RequestParam ReportStatus status,
            @AuthenticationPrincipal AuthenticatedUser admin,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reportsService.handleReport(ReportsDTO.builder()
                    .id(id)
                    .status(status)
                    .adminId(admin.getUser().getId())
                    .build());
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/report";
        }

        redirectAttributes.addFlashAttribute("message", "신고 처리 상태 변경이 완료되었습니다.");
        return "redirect:/admin/report";
    }

}

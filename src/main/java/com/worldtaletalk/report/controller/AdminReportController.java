package com.worldtaletalk.report.controller;

import com.worldtaletalk.chat.domain.ChatMessagesEntity;
import com.worldtaletalk.chat.repository.ChatMessagesRepository;
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
    private final ChatMessagesRepository chatMessagesRepository;

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
            @RequestParam(required = false) String status,
            Model model
    ) {
        page = Math.max(page, 1);
        String selectedStatus = status == null ? "PENDING" : status;
        ReportStatus statusFilter = selectedStatus.isBlank() ? null : ReportStatus.valueOf(selectedStatus);
        Page<ReportsDTO> reports = reportsService.getReports(statusFilter, page);
        model.addAttribute("reports", reports.getContent());
        model.addAttribute("reportPage", reports);
        model.addAttribute("status", statusFilter);
        model.addAttribute("selectedStatus", selectedStatus);
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

    /**
     * ART-03: 신고된 리뷰 삭제 후 신고 처리 완료
     *
     * @param id 신고 PK
     * @param admin 현재 로그인한 관리자
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/report (신고 목록 페이지)
     */
    @PostMapping("/{id}/review/delete")
    public String deleteReportedReview(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser admin,
            RedirectAttributes redirectAttributes
    ) {
        try {
            reportsService.deleteReportedReview(id, admin.getUser().getId());
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/report";
        }

        redirectAttributes.addFlashAttribute("message", "리뷰 삭제 및 신고 처리가 완료되었습니다.");
        return "redirect:/admin/report";
    }

    /**
     * ART-04: 채팅 신고 맥락 확인
     *
     * @param messageId 신고된 채팅 메시지 PK
     * @param model 뷰에 전달할 모델
     * @return admin/report/chat-context (채팅 신고 맥락 확인 페이지)
     */
    @GetMapping("/chat/{messageId}")
    public String chatContext(@PathVariable Long messageId, Model model) {
        ChatMessagesEntity targetMessage = chatMessagesRepository.findByIdWithStorySessionDetails(messageId).orElseThrow(() ->
                new NoSuchElementException("해당 채팅 메시지가 존재하지 않습니다."));
        Long sessionId = targetMessage.getStorySession().getId();

        model.addAttribute("targetMessage", targetMessage);
        model.addAttribute("storySession", targetMessage.getStorySession());
        model.addAttribute("messages", chatMessagesRepository.findAllByStorySessionId(sessionId));
        return "admin/report/chat-context";
    }

}

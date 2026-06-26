package com.worldtaletalk.report.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.report.domain.ReportTargetType;
import com.worldtaletalk.report.dto.ReportsDTO;
import com.worldtaletalk.report.service.ReportsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportViewController {

    private final ReportsService reportsService;

    // 신고 등록 요청 폼 - 신고 타입, 대상 ID, 사유 세 필드를 하나의 레코드로 처리함
    private record ReportForm(
            @NotNull(message = "신고할 타입을 선택해주세요.")
            ReportTargetType targetType,

            @NotNull(message = "신고할 대상을 선택하세요.")
            Long targetId,

            @NotBlank(message = "신고사유를 입력해주세요.")
            String reason
    ) {
    }

    /**
     * RP-01: 신고 폼
     *
     * @param targetType 신고 대상 종류
     * @param targetId   신고 대상 PK
     * @param model      뷰에 전달할 모델
     * @return report/form (신고 폼 페이지)
     */
    @GetMapping("/new")
    public String reportForm(@RequestParam ReportTargetType targetType,
                             @RequestParam Long targetId,
                             Model model
    ) {
        model.addAttribute("targetType", targetType);
        model.addAttribute("targetId", targetId);
        return "report/form";
    }

    /**
     * RP-02: 신고 등록
     *
     * @param user               현재 로그인 유저
     * @param form               신고 폼
     * @param bindingResult      폼 검증 결과
     * @param request            HTTP 요청 (Referer 헤더 추출용)
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:Referer (신고 이전 페이지) 또는 redirect:/ (홈 페이지)
     */
    @PostMapping
    public String report(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute ReportForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/reports/new?targetType=" + form.targetType() + "&targetId=" + form.targetId();
        }

        reportsService.addReport(ReportsDTO.builder()
                .userId(user.getId())
                .targetType(form.targetType())
                .targetId(form.targetId())
                .reason(form.reason())
                .build()
        );
        redirectAttributes.addFlashAttribute("message", "신고 접수가 완료되었습니다.");

        // 신고는 어떤 페이지든 가능함 그래서 referer를 사용
        String referer = request.getHeader("Referer");
        String serverName = request.getServerName();

        // 직접적으로 referer를 사용하기 때문에 외부 도메인이 들어올 수 도 있음 현재 서버 도메인인지 검증
        if (referer != null && referer.contains(serverName)) {
            return "redirect:" + referer;
        }
        return "redirect:/";
    }
}

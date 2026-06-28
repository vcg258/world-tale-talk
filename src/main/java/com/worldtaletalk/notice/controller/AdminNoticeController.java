package com.worldtaletalk.notice.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.notice.dto.NoticesDTO;
import com.worldtaletalk.notice.service.NoticesService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticesService noticesService;

    // 공지사항 등록/수정 요청 폼 - 제목, 내용, 활성화 여부 세 필드를 하나의 레코드로 처리함
    private record NoticeForm(
            @NotBlank(message = "공지 제목을 입력해주세요.")
            @Size(max = 50, message = "제목은 최대 50자 이내로 입력해주세요.")
            String title,

            @NotBlank(message = "공지 내용을 입력해주세요.")
            String content,

            @NotNull(message = "활성화 여부를 입력해주세요.")
            Boolean isActive
    ) {
    }

    /**
     * AN-01: 공지 목록 조회
     *
     * @param page  페이지 번호
     * @param model 뷰에 전달할 모델
     * @return admin/notices/list (공지 목록 페이지)
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page, Model model) {
        Page<NoticesDTO> notices = noticesService.getAllNotices(page);
        model.addAttribute("notices", notices.getContent());
        model.addAttribute("noticesPage", notices);
        return "admin/notices/list";
    }

    /**
     * AN-02: 공지 등록 폼
     *
     * @return admin/notices/form (공지 등록 폼 페이지)
     */
    @GetMapping("/new")
    public String noticeForm(Model model) {
        model.addAttribute("notice", NoticesDTO.builder().build()); // TODO 협의 필요 모델에 아무것도 넘기지 않았기때문에 NPE 방지 필요시 수정
        return "admin/notices/form";
    }

    /**
     * AN-03: 공지 수정 폼
     *
     * @param id    공지 PK
     * @param model 뷰에 전달할 모델
     * @return admin/notices/form (공지 수정 폼 페이지)
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("notice", noticesService.getNotice(id));
        return "admin/notices/form";
    }

    /**
     * AN-04: 공지 등록
     *
     * @param admin              현재 로그인한 관리자
     * @param form               공지 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/notices (공지 목록 페이지)
     */
    @PostMapping
    public String create(
            @AuthenticationPrincipal AuthenticatedUser admin,
            @Valid @ModelAttribute NoticeForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/notices/new";
        }
        noticesService.createNotice(NoticesDTO.builder()
                .adminId(admin.getUser().getId())
                .title(form.title())
                .content(form.content())
                .active(form.isActive())
                .build());
        return "redirect:/admin/notices";
    }

    /**
     * AN-05: 공지 수정
     *
     * @param id                 공지 PK
     * @param admin              현재 로그인한 관리자
     * @param form               공지 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/notices (공지 목록 페이지)
     */
    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser admin,
            @Valid @ModelAttribute NoticeForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/admin/notices/" + id + "/edit";
        }
        noticesService.modifyNotice(NoticesDTO.builder()
                .id(id)
                .adminId(admin.getUser().getId())
                .title(form.title())
                .content(form.content())
                .active(form.isActive())
                .build());
        return "redirect:/admin/notices";
    }

    /**
     * AN-06: 공지 삭제
     *
     * @param id 공지 PK
     * @return redirect:/admin/notices (공지 목록 페이지)
     */
    @PostMapping("/{id}")
    public String delete(@PathVariable Long id) {
        noticesService.deleteNotice(id);
        return "redirect:/admin/notices";
    }
}

package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.global.util.BindingResultUtils;
import com.worldtaletalk.user.domain.user.UserStatus;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.dto.UserSuspensionsDTO;
import com.worldtaletalk.user.service.SocialAccountsService;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.user.service.UserSuspensionsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final UserSuspensionsService userSuspensionsService;
    private final SocialAccountsService socialAccountsService;

    // 유저 정지 요청 폼 - 정지 사유 단일 필드를 레코드로 처리함
    private record SuspendForm(@NotBlank(message = "정지사유를 입력해주세요.") String reason) {
    }

    /**
     * AU-01: 회원 목록 조회 (탈퇴 회원 제외)
     *
     * @param q      검색어 (닉네임 부분 일치, null이면 전체 조회)
     * @param status 회원 상태 필터 (null이면 전체 조회)
     * @param page   페이지 번호
     * @param model  뷰에 전달할 모델
     * @return admin/users/list (회원 목록 페이지)
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        Page<UserDTO> users = userService.getUsers(q, status, page);
        model.addAttribute("users", users.getContent());
        model.addAttribute("usersPage", users);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        return "admin/users/list";
    }

    /**
     * AU-01-1: 회원 목록 조회 (탈퇴 회원만)
     *
     * @param q      검색어 (닉네임 부분 일치, null이면 전체 조회)
     * @param page   페이지 번호
     * @param model  뷰에 전달할 모델
     * @return admin/users/list-del (탈퇴 회원 목록 페이지)
     */
    @GetMapping("/del")
    public String delList(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        Page<UserDTO> users = userService.getDelUsers(q, page);
        model.addAttribute("users", users.getContent());
        model.addAttribute("usersPage", users);
        model.addAttribute("q", q);
        return "admin/users/list-del";
    }

    /**
     * AU-02: 회원 상세 조회
     *
     * @param id    회원 PK
     * @param model 뷰에 전달할 모델
     * @return admin/users/detail (회원 상세 페이지)
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUser(id));
        model.addAttribute("socialAccounts", socialAccountsService.getSocialAccountsByUserId(id));
        model.addAttribute("suspensions", userSuspensionsService.getAllUserSuspensions(id));
        return "admin/users/detail";
    }

    /**
     * AU-03: 회원 정지 처리
     *
     * @param id                 회원 PK
     * @param admin              현재 로그인한 관리자
     * @param form               정지 사유 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/admin/users/{id} (회원 상세 페이지)
     */
    @PostMapping("/{id}/suspend")
    public String suspend(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser admin,
            @Valid @ModelAttribute SuspendForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error",
                    BindingResultUtils.getFirstErrorMessage(bindingResult, "입력값을 확인해주세요."));
            return "redirect:/admin/users/" + id;
        }
        userSuspensionsService.addUserSuspension(UserSuspensionsDTO.builder()
                .userId(id)
                .adminId(admin.getUser().getId())
                .reason(form.reason())
                .build()
        );
        return "redirect:/admin/users/" + id;
    }

    /**
     * AU-04: 회원 정지 해제
     *
     * @param id 회원 PK
     * @return redirect:/admin/users/{id} (회원 상세 페이지)
     */
    @PostMapping("/{id}/lift")
    public String lift(@PathVariable Long id) {
        UserSuspensionsDTO userSuspension = userSuspensionsService.getUserSuspension(id);
        userSuspensionsService.liftUserSuspension(userSuspension.getId());
        return "redirect:/admin/users/" + id;
    }

    /**
     * AU-05: 회원 강제 탈퇴
     *
     * @param id 회원 PK
     * @return redirect:/admin/users (회원 목록 페이지)
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}

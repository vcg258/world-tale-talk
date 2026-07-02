package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.mail.EmailOtpService;
import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.global.security.SecurityContextUtil;
import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.SocialAccountsService;
import com.worldtaletalk.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {
    private final UserService userService;
    private final StorySessionsService storySessionsService;
    private final WorldRatingsService worldRatingsService;
    private final EmailOtpService emailOtpService;
    private final SocialAccountsService socialAccountsService;

    // 마이페이지 프로필 수정 요청 폼 - 닉네임 단일 필드를 레코드로 처리함
    private record ProfileForm(@NotBlank(message = "닉네임을 입력해주세요.") String nickname) {}

    // 마이페이지 비밀번호 변경 요청 폼 - 현재 비밀번호, 새 비밀번호, 새 비밀번호 확인 세 필드를 하나의 레코드로 처리함
    private record PasswordForm(
            @NotBlank(message = "현재 비밀번호를 입력해주세요.")
            String currentPassword,

            @NotBlank(message = "새 비밀번호를 입력해주세요.")
            @Size(min = 8, max = 50, message = "비밀번호는 8~50자로 입력해주세요.")
            @Pattern(
                    regexp = "^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]*$",
                    message = "비밀번호는 영문, 숫자, 특수문자만 사용할 수 있습니다."
            )
            String newPassword,

            @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
            String confirmPassword
    ) {}

    // 마이페이지 스토리 목록 뷰 모델 - 세션 ID, 세계관명, 페르소나명, 상태, 턴수, 생성일을 묶어 타임리프에 전달함
    private record StorySessionListItemVM(
            Long id,
            String worldName,
            String personaName,
            SessionStatus status,
            int currentTurn,
            LocalDateTime createdAt
    ) {}

    // 마이페이지 이메일 변경 요청 폼 - 이메일 단일 필드를 레코드로 처리함
    private record EmailChangeRequestForm(@NotBlank @Email String newEmail) {}

    // 마이페이지 OTP 검증 폼 - 변경할 이메일, OTP, 현재 비밀번호를 하나의 레코드로 처리함
    private record EmailVerifyForm(@NotBlank String newEmail, @NotBlank String code, @NotBlank String currentPassword) {}

    /**
     * M-01: 마이페이지 메인
     *
     * @param user  현재 로그인 유저
     * @param model 뷰에 전달할 모델
     * @return mypage/index (마이페이지 메인 페이지)
     */
    @GetMapping
    public String index(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
        UserDTO userDTO = userService.getUser(user.getUser().getId());
        Page<StorySessionsDTO> mySessions = storySessionsService.getMySessions(user.getUser().getId(), null, 1);
        List<StorySessionListItemVM> recentSessions = mySessions.stream()
                        .map(r -> new StorySessionListItemVM(
                                r.getId(), r.getWorldName(), r.getPersonaName(), r.getStatus(),
                                r.getCurrentTurn(), r.getCreatedAt()
                        )).toList();
        model.addAttribute("user", userDTO);
        model.addAttribute("recentSessions", recentSessions);
        return "mypage/index";
    }

    /**
     * M-02: 프로필 조회
     *
     * @param user  현재 로그인 유저
     * @param model 뷰에 전달할 모델
     * @return mypage/profile (프로필 페이지)
     */
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
        model.addAttribute("user", userService.getUser(user.getUser().getId()));
        model.addAttribute("socialAccounts", socialAccountsService.getSocialAccountsByUserId(user.getUser().getId()));
        return "mypage/profile";
    }

    /**
     * M-03: 닉네임 변경
     *
     * @param user               현재 로그인 유저
     * @param form               닉네임 변경 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @param request            세션 갱신 요청
     * @return redirect:/mypage/profile (프로필 페이지)
     */
    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute ProfileForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getFieldError() != null
                    ? bindingResult.getFieldError().getDefaultMessage()
                    : "입력값을 확인해주세요.";
            redirectAttributes.addFlashAttribute(
                    "errors", errorMsg);
            return "redirect:/mypage/profile";
        }

        try {
            userService.modifyNickname(user.getUser().getId(), form.nickname());
        } catch (IllegalArgumentException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("errors", e.getMessage());
            return "redirect:/mypage/profile";
        }

        // 닉네임 변경후 세션 갱신
        UserDTO updateUser = userService.getUser(user.getUser().getId());
        SecurityContextUtil.refresh(user, updateUser, request.getSession());

        redirectAttributes.addFlashAttribute("message", "닉네임이 변경되었습니다");
        return "redirect:/mypage/profile";
    }

    /**
     * M-03-1: 비밀번호 변경 (일반 회원 전용, 소셜 전용 계정은 대상 아님 어차피 일반 회원에서 소셜 연동할 경우 일반 회원이 되기때문임)
     *
     * @param user               현재 로그인 유저
     * @param form               비밀번호 변경 폼 (현재 비밀번호, 새 비밀번호, 새 비밀번호 확인)
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @param request            세션 갱신 요청
     * @return redirect:/mypage/profile (프로필 페이지)
     */
    @PostMapping("/password")
    public String updatePassword(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute PasswordForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/mypage/profile";
        }

        try {
            userService.modifyPassword(user.getUser().getId(), form.newPassword(), form.currentPassword(),  form.confirmPassword());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errors", e.getMessage());
            return "redirect:/mypage/profile";
        }

        // 비밀번호 변경후 세션 갱신 (재로그인 없이 세션 유지)
        UserDTO updateUser = userService.getUser(user.getUser().getId());
        SecurityContextUtil.refresh(user, updateUser, request.getSession());

        redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다");
        return "redirect:/mypage/profile";
    }

    /**
     * M-03-2: 이메일 변경 - 1단계 새 이메일로 인증코드 발송
     *
     * @param user               현재 로그인 유저
     * @param form               이메일 변경 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/mypage/profile (프로필 페이지)
     */
    @PostMapping("/email/send-code")
    public String sendEmailChangeCode(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute EmailChangeRequestForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/mypage/profile";
        }

        try {
            emailOtpService.sendOtp(user.getUser().getId(), form.newEmail());
            redirectAttributes.addFlashAttribute("message", "인증코드를 발송했습니다.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errors", e.getMessage());
            return "redirect:/mypage/profile";
        }
        return "redirect:/mypage/profile";
    }

    /**
     * M-03-3: 이메일 변경 - 2단계 인증코드 검증 + 현재 비밀번호 재확인 후 실제 변경
     *
     * @param user               현재 로그인 유저
     * @param form               이메일 변경 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @param request            세션 갱신 요청
     * @return redirect:/mypage/profile (프로필 페이지)
     */
    @PostMapping("/email/verify")
    public String verifyAndChangeEmail(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute EmailVerifyForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors",
                    bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/mypage/profile";
        }

        // 비밀번호 재확인 먼저 검증
        try {
            userService.verifyCurrentPassword(user.getUser().getId(), form.currentPassword());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errors", e.getMessage());
            return "redirect:/mypage/profile";
        }

        // 2. OTP 검증 (여기서부터 소모됨)
        if (!emailOtpService.verifyOtp(user.getUser().getId(), form.newEmail(), form.code())) {
            redirectAttributes.addFlashAttribute("errors", "인증코드가 일치하지 않거나 만료되었습니다.");
            return "redirect:/mypage/profile";
        }

        // 변경
        try {
            userService.modifyEmail(user.getUser().getId(), form.newEmail(), form.currentPassword());
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errors", e.getMessage());
            return "redirect:/mypage/profile";
        }
        UserDTO updateUser = userService.getUser(user.getUser().getId());
        SecurityContextUtil.refresh(user, updateUser, request.getSession());
        redirectAttributes.addFlashAttribute("message", "이메일이 변경되었습니다");
        return "redirect:/mypage/profile";
    }

    /**
     * M-04: 회원 탈퇴
     *
     * @param user 현재 로그인 유저
     * @return redirect:/logout (로그아웃)
     */
    @PostMapping("/withdraw")
    public String withdraw(
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        userService.deleteUser(user.getUser().getId());

        // 세션 무효화 SecurityContext 초기화를 Security가 처리
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth); // 직접 세션 무효화

        return "redirect:/auth/login"; // 로그아웃 페이지로 굳이 이동할 필요는 없음
    }

    /**
     * M-05: 내 세션 목록
     *
     * @param user   현재 로그인 유저
     * @param status 세션 상태 필터 (null이면 전체 조회)
     * @param page   페이지 번호
     * @param model  뷰에 전달할 모델
     * @return mypage/sessions (내 세션 목록 페이지)
     */
    @GetMapping("/sessions")
    public String sessions(@AuthenticationPrincipal AuthenticatedUser user,
                           @RequestParam(required = false) SessionStatus status,
                           @RequestParam(defaultValue = "1") int page,
                           Model model
    ) {
        Page<StorySessionsDTO> mySessions = storySessionsService.getMySessions(user.getUser().getId(), status, page);
        List<StorySessionListItemVM> recentSessions = mySessions.stream()
                .map(r -> new StorySessionListItemVM(
                        r.getId(), r.getWorldName(), r.getPersonaName(), r.getStatus(),
                        r.getCurrentTurn(), r.getCreatedAt()
                )).toList();
        model.addAttribute("sessions", recentSessions);
        model.addAttribute("status", status);
        model.addAttribute("sessionPage", mySessions);
        return "mypage/sessions";
    }

    /**
     * M-06: 내 별점 목록
     *
     * @param user  현재 로그인 유저
     * @param page  페이지 번호
     * @param model 뷰에 전달할 모델
     * @return mypage/ratings (내 별점 목록 페이지)
     */
    @GetMapping("/ratings")
    public String ratings(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        Page<WorldRatingsDTO> myRatings = worldRatingsService.getMyRatings(user.getUser().getId(), page);
        model.addAttribute("ratings", myRatings.getContent());
        model.addAttribute("ratingsPage", myRatings);
        return "mypage/ratings";

    }

    /**
     * M-07: 소셜 계정 연동 해제
     *
     * @param user               현재 로그인 유저
     * @param socialAccountId    해제할 소셜 계정 PK
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/mypage/profile (프로필 페이지)
     */
    @PostMapping("/social/{socialAccountId}")
    public String unlinkSocialAccount(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable Long socialAccountId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            socialAccountsService.deleteSocialAccount(socialAccountId, user.getUser().getId());
        } catch (NoSuchElementException  | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errors", e.getMessage());
            return "redirect:/mypage/profile";
        }
        redirectAttributes.addFlashAttribute("message", "소셜 계정 연동이 해제되었습니다.");
        return "redirect:/mypage/profile";
    }
}

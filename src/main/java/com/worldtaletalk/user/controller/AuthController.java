package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.mail.FindIdService;
import com.worldtaletalk.global.mail.PasswordResetService;
import com.worldtaletalk.global.mail.SignupEmailOtpService;
import com.worldtaletalk.global.mail.SignupEmailOtpRedisRepository;
import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.global.security.CustomUserDetails;
import com.worldtaletalk.global.security.SecurityContextUtil;
import com.worldtaletalk.global.security.oauth2.CustomOAuth2User;
import com.worldtaletalk.global.util.BindingResultUtils;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.user.service.UserSuspensionsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserSuspensionsService userSuspensionsService;
    private final PasswordResetService passwordResetService;
    private final FindIdService findIdService;
    private final SignupEmailOtpService signupEmailOtpService;

    // 닉네임 설정 요청 폼 - 닉네임 단일 필드를 레코드로 처리함
    private record NicknameForm(@NotBlank(message = "닉네임을 입력해주세요.") String nickname) {}

    // 회원가입 요청 폼 - 아이디, 닉네임, 이메일, 비밀번호, 비밀번호 확인, 약관/개인정보 수집 동의 여부를 레코드로 처리
    private record SignupForm(
            @NotBlank(message = "아이디를 입력해주세요.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])[a-zA-Z0-9]{4,30}$", message = "아이디는 영문/숫자 4~30자로 입력해주세요.")
            String loginId,

            @NotBlank(message = "닉네임을 입력해주세요.")
            String nickname,

            @NotBlank(message = "이메일을 입력해주세요.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email,

            @NotBlank(message = "비밀번호를 입력해주세요.")
            @Size(min = 8, max = 50, message = "비밀번호는 8~50자로 입력해주세요.")
            String password,

            @NotBlank(message = "비밀번호 확인을 입력해주세요.")
            String confirmPassword,

            @NotNull(message = "이용약관에 동의해주세요.")
            @AssertTrue(message = "이용약관에 동의해주세요.")
            Boolean agreeTerms,

            @NotNull(message = "개인정보 수집·이용에 동의해주세요.")
            @AssertTrue(message = "개인정보 수집·이용에 동의해주세요.")
            Boolean agreePrivacy
    ) {}

    // 비밀번호 찾기 요청 폼 - 아이디와 이메일을 받아 레코드로 처리
    private record PasswordResetRequestForm(
            @NotBlank(message = "아이디를 입력해주세요.") String loginId,
            @NotBlank(message = "이메일을 입력해주세요.") @Email(message = "이메일 형식이 올바르지 않습니다.") String email
    ) {}

    // 비밀번호 찾기 폼 - 조회 했을시 회원이 존재했을시 토큰(재설정 URL)과 비밀번호, 비밀번호 확인을 레코드로 처리
    private record PasswordResetForm(
            @NotBlank String token,
            @NotBlank @Size(min = 8, max = 50, message = "비밀번호는 8~50자로 입력해주세요.") String newPassword,
            @NotBlank String confirmPassword
    ) {}

    // 아이디 찾기 요청 폼 - 이메일 단일 필드를 레코드로 처리
    private record FindIdRequestForm(
            @NotBlank(message = "이메일을 입력해주세요.")
            @Email(message = "이메일 형식이 올바르지 않습니다.")
            String email
    ) {}

    /**
     * A-01: 로그인 화면 조회
     *
     * @return auth/login (로그인 페이지)
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /**
     * A-02: 닉네임 실시간 중복 검사 (REST)
     *
     * @param nickname 닉네임
     * @return 닉네임 검증 결과
     */
    @GetMapping("/nickname/check")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkNickname(@RequestParam("nickname") String nickname) {
        return ResponseEntity.ok().body(Map.of("available", !userService.existsByNickname(nickname)));
    }

    /**
     * A-03: 닉네임 입력 화면 조회
     *
     * @return auth/nickname (닉네임 입력 페이지)
     */
    @GetMapping("nickname")
    public String nicknameForm() {
        return "auth/nickname";
    }

    /**
     * A-04: 닉네임 저장
     *
     * @param customOAuth2User 로그인 회원 정보
     * @param form             닉네임 폼
     * @param bindingResult    폼 검증 결과
     * @param model            뷰에 전달할 모델
     * @return redirect:/ (홈 페이지) 또는 auth/nickname (닉네임 입력 페이지)
     */
    @PostMapping("nickname")
    public String submitNickname(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                 @Valid @ModelAttribute NicknameForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("nicknameError", BindingResultUtils.getFirstErrorMessage(bindingResult, "입력값을 확인해주세요."));
            return "auth/nickname";
        }

        try {
            userService.modifyNickname(customOAuth2User.getUser().getId(), form.nickname());
        } catch (IllegalArgumentException | NoSuchElementException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/nickname";
        }

        // 세션 갱신
        UserDTO updateUser = userService.getUser(customOAuth2User.getUser().getId());
        SecurityContextUtil.refresh(customOAuth2User, updateUser, request.getSession());
        return "redirect:/";

    }

    /**
     * A-05: 정지 안내 화면 조회
     *
     * @param authenticatedUser 로그인 회원 정보
     * @param loginId           일반 로그인 실패로 유입된 경우의 로그인 아이디 (OAuth2 경유시 null)
     * @param model             뷰에 전달할 모델
     * @return auth/suspended (정지 안내 페이지)
     */
    @GetMapping("/suspended")
    public String suspended(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                            @RequestParam(required = false) String loginId,
                            Model model
    ) {

        // 정지 사유를 실패 핸들러에 전달해주기 위함
        Long userId = null;
        if (authenticatedUser != null) {
            userId = authenticatedUser.getUser().getId();
        } else if (loginId != null) {
            UserDTO user = userService.getUserByLoginId(loginId);
            userId = user != null ? user.getId() : null;
        }

        if (userId != null) {
            try {
                model.addAttribute("suspended", userSuspensionsService.getUserSuspension(userId));
            } catch (NoSuchElementException ignored) {}
        }
        return "auth/suspended";
    }

    /**
     * A-05-1: 휴면 안내 화면 조회
     *
     * @param authenticatedUser 로그인 회원 정보
     * @param model             뷰에 전달할 모델
     * @return auth/dormant (휴면 안내 페이지)
     */
    @GetMapping("/dormant")
    public String dormant(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                            Model model
    ) {
        Long userId = null;
        if (authenticatedUser != null) {
            userId = authenticatedUser.getUser().getId();
        }

        if (userId != null) {
            try {
                model.addAttribute("dormant", userService.getUser(userId));
            } catch (NoSuchElementException ignored) {}
        }
        return "auth/dormant";
    }

    /**
     * A-05-2: 휴면 계정 해제 (본인 재로그인을 통한 자기 자신 활성화)
     *
     * @param authenticatedUser 로그인 회원 정보
     * @return redirect:/ (메인 화면)
     */
    @PostMapping("/dormant/reactivate")
    public String reactivateDormant(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        userService.reactivateUser(authenticatedUser.getUser().getId());
        return "redirect:/";
    }

    /**
     * A-06: 회원가입 화면 조회
     *
     * @return auth/signup (회원가입 페이지)
     */
    @GetMapping("/signup")
    public String signupForm() {
        return "auth/signup";
    }

    /**
     * A-07: 회원가입 처리
     *
     * @param form          회원가입 폼
     * @param bindingResult 폼 검증 결과
     * @param model         뷰에 전달할 모델
     * @return redirect:/auth/login (로그인 페이지) 또는 auth/signup (회원가입 페이지)
     */
    @PostMapping("/signup")
    public String signup(
            @Valid @ModelAttribute SignupForm form,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", BindingResultUtils.getFirstErrorMessage(bindingResult, "입력값을 확인해주세요."));
            return "auth/signup";
        }

        if (!signupEmailOtpService.isVerified(form.email())) {
            model.addAttribute("error", "이메일 인증을 먼저 완료 해주세요.");
            return "auth/signup";
        }

        UserDTO createdUser;
        try {
            createdUser = userService.signUp(UserDTO.builder()
                    .loginId(form.loginId())
                    .nickname(form.nickname())
                    .email(form.email())
                    .password(form.password())
                    .build(), form.confirmPassword()
            );
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/signup";
        }

        SecurityContextUtil.login(new CustomUserDetails(createdUser), request);
        return "redirect:/";
    }

    /**
     * A-08: 아이디 실시간 중복 검사 (REST)
     *
     * @param loginId 로그인 아이디
     * @return 아이디 검증 결과
     */
    @GetMapping("/signup/check-id")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkLoginId(@RequestParam("loginId") String loginId) {
        return ResponseEntity.ok().body(Map.of("available", !userService.existsByLoginId(loginId)));
    }

    /**
     * A-09: 이메일 실시간 중복 검사 (REST)
     *
     * @param email 이메일
     * @return 이메일 검증 결과
     */
    @GetMapping("/signup/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam("email") String email) {
        return ResponseEntity.ok().body(Map.of("available", !userService.existsByEmail(email)));
    }

    /**
     * A-09-1: 회원가입 이메일 본인확인 인증번호 발송 (REST)
     *
     * @param email 이메일
     * @return 이메일 발송 결과
     */
    @PostMapping("/signup/send-code")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendSignupOtp(@RequestParam("email") String email) {
        try {
            signupEmailOtpService.signupSendOtp(email);
            return ResponseEntity.ok().body(Map.of(
                    "success", true,
                    "message", "인증코드를 발송했습니다.",
                    "resendAfterSeconds", SignupEmailOtpRedisRepository.RESEND_COOLDOWN.toSeconds(),
                    "expiresInSeconds", SignupEmailOtpRedisRepository.OTP_TTL.toSeconds()
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.ok().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * A-09-2: 회원가입 이메일 인증번호 검증 (REST)
     *
     * @param email 이메일
     * @return 이메일 검증 결과
     */
    @PostMapping("/signup/verify-code")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> verifySignupOtp(
            @RequestParam("email") String email,
            @RequestParam("otp") String otp
    ) {
        boolean verifyOtp = signupEmailOtpService.verifyOtp(email, otp);
        return ResponseEntity.ok().body(Map.of("verifyOtp", verifyOtp));
    }

    /**
     * A-10: 비밀번호 찾기 화면 조회
     *
     * @return /auth/find-password (비밀번호 재설정 페이지)
     */
    @GetMapping("/find-password")
    public String findPasswordForm() {
        return "auth/find-password";
    }

    /**
     * A-11: 비밀번호 찾기 URL 메일 발송
     *
     * @param form 비밀번호 재설정 요청 폼
     * @param bindingResult 폼검증 결과
     * @param model 뷰에 전달할 모델
     * @return /auth/reset-password (비밀번호 재설정 페이지)
     */
    @PostMapping("/find-password")
    public String sendResetLink(
            @Valid @ModelAttribute PasswordResetRequestForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", BindingResultUtils.getFirstErrorMessage(bindingResult, "입력값을 확인해주세요."));
            return "auth/find-password";
        }
        passwordResetService.sendResetLink(form.loginId(), form.email());
        model.addAttribute("message", "가입된 계정이 있다면 이메일로 재설정 링크를 안내해드립니다.");
        return "auth/find-password";
    }

    /**
     * A-12: 비밀번호 재설정 화면 조회
     *
     * @param token 비밀번호 재설정 토큰
     * @param model 뷰에 전달할 모델
     * @return auth/reset-password (비밀번호 재설정 페이지) 또는 auth/find-password (토큰이 유효하지 않을 시)
     */
    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        try {
            passwordResetService.verifyToken(token);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/find-password";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    /**
     * A-13: 비밀번호 재설정 처리
     *
     * @param form          비밀번호 재설정 폼
     * @param bindingResult 폼 검증 결과
     * @param model         뷰에 전달할 모델
     * @return redirect:/auth/login (로그인 페이지) 또는 auth/reset-password (비밀번호 재설정 페이지)
     */
    @PostMapping("/reset-password")
    public String resetPassword(
            @Valid @ModelAttribute PasswordResetForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("token", form.token());
            model.addAttribute("error", BindingResultUtils.getFirstErrorMessage(bindingResult, "입력값을 확인해주세요."));
            return "auth/reset-password";
        }
        try {
            Long userId = passwordResetService.verifyToken(form.token());
            userService.resetPassword(userId, form.newPassword(), form.confirmPassword());
        } catch (IllegalArgumentException | NoSuchElementException e) {
            model.addAttribute("token", form.token());
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }

        passwordResetService.consumeToken(form.token());
        return "redirect:/auth/login";
    }

    /**
     * A-14: 아이디 찾기 화면 조회
     *
     * @return auth/find-id (아이디 찾기 페이지)
     */
    @GetMapping("/find-id")
    public String findIdForm() {
        return "auth/find-id";
    }

    /**
     * A-15: 아이디 찾기 메일 발송
     *
     * @param form          아이디 찾기 요청 폼
     * @param bindingResult 폼 검증 결과
     * @param model         뷰에 전달할 모델
     * @return auth/find-id (아이디 찾기 페이지)
     */
    @PostMapping("/find-id")
    public String sendFindIdEmail(
            @Valid @ModelAttribute FindIdRequestForm form,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", BindingResultUtils.getFirstErrorMessage(bindingResult, "입력값을 확인해주세요."));
            return "auth/find-id";
        }

        findIdService.sendLoginId(form.email());
        model.addAttribute("message", "가입된 계정이 있다면 이메일로 아이디를 안내해드립니다.");
        return "auth/find-id";
    }
}
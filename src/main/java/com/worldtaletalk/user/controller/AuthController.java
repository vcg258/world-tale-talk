package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.global.security.CustomUserDetails;
import com.worldtaletalk.global.security.SecurityContextUtil;
import com.worldtaletalk.global.security.oauth2.CustomOAuth2User;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.user.service.UserSuspensionsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    // 닉네임 설정 요청 폼 - 닉네임 단일 필드를 레코드로 처리함
    private record NicknameForm(@NotBlank(message = "닉네임을 입력해주세요.") String nickname) {
    }

    private record SignupForm(
            @NotBlank(message = "아이디를 입력해주세요.")
            @Pattern(regexp = "^[a-zA-Z0-9]{4,30}$", message = "아이디는 영문/숫자 4~30자로 입력해주세요.")
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
            String confirmPassword
    ) {
    }

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
            model.addAttribute("nicknameError", bindingResult.getFieldError("nickname").getDefaultMessage());
            return "auth/nickname";
        }

        try {
            userService.modifyNickname(customOAuth2User.getUser().getId(), form.nickname());
        } catch (IllegalArgumentException | NoSuchElementException e) {
            model.addAttribute("errors", e.getMessage());
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
     * @param model             뷰에 전달할 모델
     * @return auth/suspended (정지 안내 페이지)
     */
    @GetMapping("/suspended")
    public String suspended(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {
        if (authenticatedUser != null) {
            try {
                model.addAttribute("suspended", userSuspensionsService.getUserSuspension(authenticatedUser.getUser().getId()));
            } catch (NoSuchElementException ignored) {
            }
        }
        return "auth/suspended";
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
            model.addAttribute("errors", bindingResult.getFieldError().getDefaultMessage());
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
            model.addAttribute("errors", e.getMessage());
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
}
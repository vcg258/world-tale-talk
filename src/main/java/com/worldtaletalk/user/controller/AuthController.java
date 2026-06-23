package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.global.security.oauth2.CustomOAuth2User;
import com.worldtaletalk.user.service.UserService;
import com.worldtaletalk.user.service.UserSuspensionsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final UserSuspensionsService userSuspensionsService;

    record NicknameForm(@NotBlank(message = "닉네임을 입력해주세요.") String nickname) {
    }

    /**
     * A-01: 로그인 화면 조회
     *
     * @return auth/login
     */
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    /**
     * A-03: 닉네임 입력 화면 조회
     *
     * @return auth/nickname
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
     * @param model            뷰에 전달할 모델
     * @return 성공 시 redirect:/ , 실패 시 auth/nickname
     */
    @PostMapping("nickname")
    public String submitNickname(@AuthenticationPrincipal CustomOAuth2User customOAuth2User,
                                 @Valid @ModelAttribute NicknameForm form,
                                 BindingResult bindingResult,
                                 Model model
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("nicknameError", bindingResult.getFieldError("nickname").getDefaultMessage());
            return "auth/nickname";
        }

        if (userService.existsByNickname(form.nickname())) {
            model.addAttribute("nicknameError", "이미 사용중인 닉네임입니다.");
            return "auth/nickname";
        }
        userService.modifyNickname(customOAuth2User.getId(), form.nickname());
        return "redirect:/";

    }

    /**
     * A-05: 정지 안내 화면 조회
     *
     * @param authenticatedUser 로그인 회원 정보
     * @param model             뷰에 전달할 모델
     * @return auth/suspended
     */
    @GetMapping("/suspended")
    public String suspended(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {
        model.addAttribute("suspended", userSuspensionsService.getUserSuspension(authenticatedUser.getId()));
        return "auth/suspended";
    }
}
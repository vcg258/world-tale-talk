package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
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

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {
    private final UserService userService;
    private final StorySessionsService storySessionsService;
    private final WorldRatingsService worldRatingsService;

    private record ProfileForm(@NotBlank(message = "닉네임을 입력해주세요.") String nickname) {}

    private record StorySessionListItemVM(
            Long id,
            String worldName,
            String personaName,
            SessionStatus status,
            int currentTurn,
            LocalDateTime createdAt
    ) {}

    /**
     * M-01: 마이페이지 메인
     *
     * @param user  현재 로그인 유저
     * @param model 뷰에 전달할 모델
     * @return mypage/index (마이페이지 메인 페이지)
     */
    @GetMapping
    public String index(@AuthenticationPrincipal AuthenticatedUser user, Model model) {
        UserDTO userDTO = userService.getUser(user.getId());
        Page<StorySessionsDTO> mySessions = storySessionsService.getMySessions(user.getId(), null, 1);
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
        model.addAttribute("user", userService.getUser(user.getId()));
        return "mypage/profile";
    }

    /**
     * M-03: 닉네임 변경
     *
     * @param user               현재 로그인 유저
     * @param form               닉네임 변경 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/mypage/profile (프로필 페이지)
     */
    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute ProfileForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "errors", bindingResult.getFieldError("nickname").getDefaultMessage());
            return "redirect:/mypage/profile";
        }
        userService.modifyNickname(user.getId(), form.nickname());
        redirectAttributes.addFlashAttribute("message", "닉네임이 변경되었습니다");
        return "redirect:/mypage/profile";
    }

    /**
     * M-04: 회원 탈퇴
     *
     * @param user 현재 로그인 유저
     * @return redirect:/logout (로그아웃)
     */
    @PostMapping("/withdraw")
    public String withdraw(@AuthenticationPrincipal AuthenticatedUser user) {
        userService.deleteUser(user.getId()); // TODO 시큐리티에서 GET /logout 허용 또는 POST /logout 리다이렉트 방식으로 처리 필요
        return "redirect:/auth/logout";
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
        Page<StorySessionsDTO> mySessions = storySessionsService.getMySessions(user.getId(), status, page);
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
        Page<WorldRatingsDTO> myRatings = worldRatingsService.getMyRatings(user.getId(), page);
        model.addAttribute("ratings", myRatings.getContent());
        model.addAttribute("ratingsPage", myRatings);
        return "mypage/ratings";

    }
}

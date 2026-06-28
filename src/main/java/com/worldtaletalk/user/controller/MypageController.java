package com.worldtaletalk.user.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.global.security.SecurityContextUtil;
import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {
    private final UserService userService;
    private final StorySessionsService storySessionsService;
    private final WorldRatingsService worldRatingsService;

    // 마이페이지 프로필 수정 요청 폼 - 닉네임 단일 필드를 레코드로 처리함
    private record ProfileForm(@NotBlank(message = "닉네임을 입력해주세요.") String nickname) {}

    // 마이페이지 스토리 목록 뷰 모델 - 세션 ID, 세계관명, 페르소나명, 상태, 턴수, 생성일을 묶어 Thymeleaf에 전달하는 레코드
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
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "errors", bindingResult.getFieldError("nickname").getDefaultMessage());
            return "redirect:/mypage/profile";
        }
        userService.modifyNickname(user.getUser().getId(), form.nickname());

        // 닉네임 변경후 세션 갱신
        UserDTO updateUser = userService.getUser(user.getUser().getId());
        SecurityContextUtil.refresh(user, updateUser, request.getSession());

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
    public String withdraw(
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        userService.deleteUser(user.getUser().getId());

        // 세션 무효화 SecurityContext 초기화를 Security가 처리
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth);

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
}

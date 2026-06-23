package com.worldtaletalk.global;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.user.domain.user.UserRole;
import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {
    private final UserService userService;

    /**
     * 공통으로 들어갈 권한 model 값 지정 (지정 해준 이유 @AuthenticationPrincipal를 일일히 다 붙여줘야하는 반복작업을 덜하기 위함)
     *
     * @param authenticatedUser 로그인 회원 (OAuth2 / 일반 로그인 공통)
     * @param model             model 값
     */
    @ModelAttribute
    public void addCommonAttributes(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {
        if (authenticatedUser != null) {
            UserDTO userDTO = userService.getUser(authenticatedUser.getId()); // PK로 조회
            model.addAttribute("isLoggedIn", true); // 로그인 여부
            model.addAttribute("isAdmin", userDTO.getRole() == UserRole.ADMIN); // 관리자 계정인지 판단
            model.addAttribute("currentUser", userDTO); // 회원 정보
        } else { // 비회원
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("isAdmin", false);
            model.addAttribute("currentUser", null);
        }
    }
}

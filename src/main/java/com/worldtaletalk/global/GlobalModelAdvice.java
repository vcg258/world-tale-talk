package com.worldtaletalk.global;

import com.worldtaletalk.user.dto.UserDTO;
import com.worldtaletalk.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
     * @param userDetails 회원 권한
     * @param model       model 값
     */
    @ModelAttribute
    public void addCommonAttributes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            UserDTO userDTO = userService.getUserByEmail(userDetails.getUsername());
            model.addAttribute("isLoggedIn", true); // 로그인 여부
            model.addAttribute("isAdmin", userDetails.getAuthorities().stream() // 관리자 계정인지 판단
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
            model.addAttribute("currentUser", userDTO); // 회원 정보
        } else { // 비회원
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("isAdmin", false);
            model.addAttribute("currentUser", null);
        }
    }
}

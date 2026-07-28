package com.worldtaletalk.rating.controller;

import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.service.WorldRatingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/ratings")
@RequiredArgsConstructor
public class AdminRatingController {

    private final WorldRatingsService worldRatingsService;

    /**
     * AR-01: 별점 목록 조회
     *
     * @param nickname 회원 닉네임 필터 (null이면 전체 조회)
     * @param worldId  세계관 PK 필터 (null이면 전체 조회)
     * @param page     페이지 번호
     * @param model    뷰에 전달할 모델
     * @return admin/ratings/list (별점 목록 페이지)
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Long worldId,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        page = Math.max(page, 1);
        Page<WorldRatingsDTO> worldRatings = worldRatingsService.getWorldRatings(worldId, nickname, page);
        model.addAttribute("ratings", worldRatings.getContent());
        model.addAttribute("ratingsPage", worldRatings);
        model.addAttribute("nickname", nickname);
        model.addAttribute("worldId", worldId);
        return "admin/ratings/list";
    }

    /**
     * AR-02: 별점 삭제
     *
     * @param id 별점 PK
     * @return redirect:/admin/ratings (별점 목록 페이지)
     */
    @PostMapping("/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            worldRatingsService.deleteWorldRating(id);
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/ratings";
        }
        redirectAttributes.addFlashAttribute("message", "별점 삭제가 완료되었습니다.");
        return "redirect:/admin/ratings";
    }
}

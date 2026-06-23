package com.worldtaletalk.rating.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.rating.dto.WorldRatingsDTO;
import com.worldtaletalk.rating.service.WorldRatingsService;
import com.worldtaletalk.world.service.WorldsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/worlds")
@RequiredArgsConstructor
public class RatingViewController {

    private final WorldRatingsService worldRatingsService;
    private final WorldsService worldsService;

    record RatingForm(
            @NotNull(message = "해당 세션의 PK를 입력해주세요.")
            Long sessionId,

            @NotNull(message = "별점을 입력해주세요.")
            @Min(value = 1, message = "별점은 1점 이상이여야 합니다.")
            @Max(value = 5, message = "별점은 5점 이하여야 합니다.")
            Short score,

            @Size(max = 200, message = "리뷰는 200자 이내로 작성해주세요.")
            String review
    ) {
    }

    /**
     * R-01: 별점 등록 폼 (세션 완료 후 진입)
     *
     * @param worldId   세계관 PK
     * @param sessionId 세션 PK
     * @param user      현재 로그인 유저
     * @param model     뷰에 전달할 모델
     * @return rating/form (별점 등록 폼 페이지)
     */
    @GetMapping("/{worldId}/ratings")
    public String ratingForm(
            @PathVariable Long worldId,
            @RequestParam Long sessionId,
            @AuthenticationPrincipal AuthenticatedUser user,
            Model model
    ) {
        model.addAttribute("world", worldsService.getWorldById(worldId));
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("existingRating", worldRatingsService.getMyRatingByWorldAndUser(worldId, user.getId()));

        return "rating/form";
    }

    /**
     * R-02: 별점 수정 폼
     *
     * @param worldId            세계관 PK
     * @param user               현재 로그인 유저
     * @param model              뷰에 전달할 모델
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return rating/form (별점 수정 폼 페이지)
     */
    @GetMapping("/{worldId}/ratings/edit")
    public String ratingEditForm(
            @PathVariable Long worldId,
            @AuthenticationPrincipal AuthenticatedUser user,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        WorldRatingsDTO existingRating = worldRatingsService.getMyRatingByWorldAndUser(worldId, user.getId());
        if (existingRating == null) {
            redirectAttributes.addFlashAttribute("error", "등록된 별점이 없습니다.");
            return "redirect:/mypage/ratings";
        }

        model.addAttribute("world", worldsService.getWorldById(worldId));
        model.addAttribute("sessionId", existingRating.getStorySessionId());
        model.addAttribute("existingRating", existingRating);
        return "rating/form";
    }

    /**
     * R-03: 별점 등록 / 수정
     * existingRating 존재 여부로 신규/수정 분기
     *
     * @param worldId            세계관 PK
     * @param user               현재 로그인 유저
     * @param form               별점 폼
     * @param bindingResult      폼 검증 결과
     * @param redirectAttributes 리다이렉트 시 전달할 플래시 메시지
     * @return redirect:/mypage/ratings (내 별점 목록 페이지)
     */
    @PostMapping("/{worldId}/ratings")
    public String saveRating(
            @PathVariable Long worldId,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute RatingForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", bindingResult.getFieldError().getDefaultMessage());
            return "redirect:/worlds/" + worldId + "/ratings?sessionId=" + form.sessionId();
        }

        WorldRatingsDTO existingRating = worldRatingsService.getMyRatingByWorldAndUser(worldId, user.getId());
        if (existingRating == null) {
            worldRatingsService.addWorldRating(WorldRatingsDTO.builder()
                    .userId(user.getId())
                    .worldId(worldId)
                    .storySessionId(form.sessionId())
                    .score(form.score())
                    .review(form.review())
                    .build());
        } else {
            worldRatingsService.modifyWorldRating(WorldRatingsDTO.builder()
                    .id(existingRating.getId())
                    .userId(user.getId())
                    .score(form.score())
                    .review(form.review())
                    .build());
        }

        redirectAttributes.addFlashAttribute("message", "별점이 저장되었습니다");
        return "redirect:/mypage/ratings";
    }
}

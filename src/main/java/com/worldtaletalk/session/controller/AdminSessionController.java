package com.worldtaletalk.session.controller;

import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/sessions")
@RequiredArgsConstructor
public class AdminSessionController {

    private final StorySessionsService storySessionsService;
    private final WorldsService worldsService;

    /**
     * AS-01: 세션 목록 조회
     *
     * @param userId  회원 PK 필터 (null이면 전체 조회)
     * @param worldId 세계관 PK 필터 (null이면 전체 조회)
     * @param status  세션 상태 필터 (null이면 전체 조회)
     * @param page    페이지 번호
     * @param model   뷰에 전달할 모델
     * @return admin/sessions/list (세션 목록 페이지)
     */
    @GetMapping
    public String list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long worldId,
            @RequestParam(required = false) SessionStatus status,
            @RequestParam(defaultValue = "1") int page,
            Model model
    ) {
        Page<StorySessionsDTO> sessions = storySessionsService.getSessions(userId, worldId, status, page);
        model.addAttribute("sessions", sessions.getContent());
        model.addAttribute("sessionsPage", sessions);
        model.addAttribute("worlds", worldsService.getAllWorlds());
        model.addAttribute("userId", userId);
        model.addAttribute("worldId", worldId);
        model.addAttribute("status", status);
        return "admin/sessions/list";
    }

    /**
     * AS-02: 세션 강제 포기
     *
     * @param id 세션 PK
     * @return redirect:/admin/sessions (세션 목록 페이지)
     */
    @PostMapping("/{id}/abandon")
    public String abandon(
            @PathVariable Long id
    ) {
        storySessionsService.modifyStorySessionByStatus(id, SessionStatus.ABANDONED);
        return "redirect:/admin/sessions";
    }
}

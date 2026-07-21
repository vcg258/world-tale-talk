package com.worldtaletalk.session.controller;

import com.worldtaletalk.chat.service.ChatMessagesService;
import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.inventory.service.SessionItemsService;
import com.worldtaletalk.persona.service.PersonasService;
import com.worldtaletalk.session.domain.SessionStatus;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.service.StorySessionsService;
import com.worldtaletalk.stat.service.SessionStatsService;
import com.worldtaletalk.story.cache.StoryCacheService;
import com.worldtaletalk.story.dto.PlayStateCacheDTO;
import com.worldtaletalk.world.service.WorldsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class StoryViewController {

    private final StorySessionsService storySessionsService;
    private final WorldsService worldsService;
    private final PersonasService personasService;
    private final ChatMessagesService chatMessagesService;
    private final SessionItemsService sessionItemsService;
    private final SessionStatsService sessionStatsService;
    private final StoryCacheService storyCacheService;

    // 플레이 화면 스탯 뷰 모델 - 5가지 스탯 수치를 묶어 타임리프에 전달하는 레코드
    private record CurrentStatsVM(
            int wisdom,
            int strength,
            int agility,
            int charisma,
            int luck
    ) {}

    /**
     * S-01: 플레이 화면 조회
     *
     * @param id    세션 PK
     * @param user  현재 로그인 유저
     * @param model 뷰에 전달할 모델
     * @return story/play (플레이 화면)
     */
    @GetMapping("/{id}/play")
    public String play(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user, Model model) {
        validateOwner(id, user);
        StorySessionsDTO storySession = storySessionsService.getStorySession(id);
        var world = worldsService.getWorldById(storySession.getWorldId());
        Map<String, Integer> stats = sessionStatsService.getCurrentStats(id);
        PlayStateCacheDTO playState = storyCacheService.getPlayState(id);
        int progressPercent = playState != null ? playState.getProgressPercent() : 0;
        if (playState != null) {
            model.addAttribute("currentStory", playState.getCurrentStory());
            model.addAttribute("choices", playState.getChoices());
        }

        List<com.worldtaletalk.chat.dto.ChatMessagesDTO> messages =
                new ArrayList<>(chatMessagesService.getChatMessages(id, null));
        messages.removeIf(message -> message.getRole() != null
                && "USER".equals(message.getRole().name())
                && "이야기를 시작해주세요.".equals(message.getContent()));
        Collections.reverse(messages);

        model.addAttribute("storySession", storySession);
        model.addAttribute("world", world);
        model.addAttribute("targetTurn", world.getTargetTurn());
        model.addAttribute("progressPercent", progressPercent);
        model.addAttribute("autoStartStory", playState == null && storySession.getCurrentTurn() == 0 && messages.isEmpty());
        model.addAttribute("persona", personasService.getPersona(storySession.getPersonaId()));
        model.addAttribute("messages", messages);
        model.addAttribute("items", sessionItemsService.getInventory(id));
        model.addAttribute("currentStats", new CurrentStatsVM(
                stats.getOrDefault("wisdom", 0),
                stats.getOrDefault("strength", 0),
                stats.getOrDefault("agility", 0),
                stats.getOrDefault("charisma", 0),
                stats.getOrDefault("luck", 0)
        ));
        return "story/play";
    }

    /**
     * S-02: 세션 포기
     *
     * @param id   세션 PK
     * @param user 현재 로그인 유저
     * @return redirect:/mypage/sessions (내 세션 목록 페이지)
     */
    @PostMapping("/{id}/abandon")
    public String abandon(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        validateOwner(id, user);
        storySessionsService.modifyStorySessionByStatus(id, SessionStatus.ABANDONED);
        storyCacheService.deleteSessionCache(id);
        return "redirect:/mypage/sessions";
    }

    /**
     * S-02-1: 세션 삭제
     *
     * @param id   세션 PK
     * @param user 현재 로그인 유저
     * @return redirect:/mypage/sessions (내 세션 목록 페이지)
     */
    @PostMapping("/{id}/hide")
    public String hide(@PathVariable Long id,
                       @AuthenticationPrincipal AuthenticatedUser user,
                       RedirectAttributes redirectAttributes
    ) {
        validateOwner(id, user);
        try {
            storySessionsService.modifyHiddenByUser(id, user.getUser().getId());
        } catch (IllegalStateException | NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/mypage/sessions";
        }
        redirectAttributes.addFlashAttribute("message", "세션이 삭제되었습니다.");
        return "redirect:/mypage/sessions";
    }

    /**
     * S-03: 엔딩 화면 조회
     *
     * @param id    세션 PK
     * @param user  현재 로그인 유저
     * @param model 뷰에 전달할 모델
     * @return story/ending (엔딩 화면)
     */
    @GetMapping("/{id}/ending")
    public String ending(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user, Model model) {
        validateOwner(id, user);
        StorySessionsDTO storySession = storySessionsService.getStorySession(id);
        model.addAttribute("storySession", storySession);
        model.addAttribute("world", worldsService.getWorldById(storySession.getWorldId()));
        model.addAttribute("persona", personasService.getPersona(storySession.getPersonaId())
        );
        model.addAttribute("endingStory", storyCacheService.getEndingStory(id));
        return "story/ending";
    }

    /**
     * S-04: 에필로그 시작
     *
     * @param id   세션 PK
     * @param user 현재 로그인 유저
     * @return redirect:/sessions/{id}/epilogue (에필로그 화면)
     */
    @PostMapping("/{id}/epilogue")
    public String epilogue(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        validateOwner(id, user);
        storySessionsService.modifyStorySessionByStatus(id, SessionStatus.EPILOGUE);
        return "redirect:/sessions/" + id + "/epilogue";
    }

    /**
     * S-05: 에필로그 화면 조회
     *
     * @param id    세션 PK
     * @param user  현재 로그인 유저
     * @param model 뷰에 전달할 모델
     * @return story/epilogue (에필로그 화면)
     */
    @GetMapping("/{id}/epilogue")
    public String epilogue(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user, Model model) {
        validateOwner(id, user);
        StorySessionsDTO storySession = storySessionsService.getStorySession(id);
        model.addAttribute("storySession", storySession);
        model.addAttribute("snapshotStats", sessionStatsService.getSnapshot(id));
        model.addAttribute("messages", chatMessagesService.getChatMessages(id, null));
        return "story/epilogue";
    }

    /**
     * S-06: 세션 완료
     *
     * @param id   세션 PK
     * @param user 현재 로그인 유저
     * @return redirect:/worlds/{worldId}/ratings (별점 등록 폼 페이지)
     */
    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser user) {
        validateOwner(id, user);
        StorySessionsDTO storySession = storySessionsService.getStorySession(id);
        storySessionsService.modifyStorySessionByStatus(storySession.getId(), SessionStatus.COMPLETED);
        return "redirect:/worlds/" + storySession.getWorldId() + "/ratings?sessionId=" + storySession.getId();
    }

    /**
     * S-07: 아이템 버리기
     *
     * @param sessionId 세션 PK
     * @param itemId    아이템 PK
     * @param user      현재 로그인 유저
     * @return redirect:/sessions/{sessionId}/play (플레이 화면)
     */
    @PostMapping("/{sessionId}/items/{itemId}/drop")
    public String drop(
            @PathVariable Long sessionId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal AuthenticatedUser user,
            RedirectAttributes redirectAttributes
    ) {
        validateOwner(sessionId, user);
        try {
            sessionItemsService.dropItem(sessionId, itemId);
        } catch (NoSuchElementException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sessions/" + sessionId + "/play";
    }


    /* Helper Method */

    /**
     * 세션 소유자 검증 (HTTP 요청용)
     *
     * @param sessionId 세션 PK
     * @param user      현재 로그인 유저
     */
    private void validateOwner(Long sessionId, AuthenticatedUser user) {
        storySessionsService.validateOwner(sessionId, user != null ? user.getUser().getId() : null);
    }
}

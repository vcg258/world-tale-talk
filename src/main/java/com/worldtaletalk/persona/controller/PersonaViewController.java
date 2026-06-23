package com.worldtaletalk.persona.controller;

import com.worldtaletalk.global.security.AuthenticatedUser;
import com.worldtaletalk.session.dto.StorySessionsDTO;
import com.worldtaletalk.session.service.StorySessionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/worlds")
@RequiredArgsConstructor
public class PersonaViewController {

    private final StorySessionsService storySessionsService;

    /**
     * P-01: 세션 생성 (새게임 시작)
     *
     * @param worldId   세계관 PK
     * @param personaId 페르소나 PK
     * @param user      현재 로그인 유저
     * @return redirect:/sessions/{sessionId}/play (플레이 화면)
     */
    @PostMapping("/{worldId}/personas/{personaId}/sessions")
    public String createSession(
            @PathVariable Long worldId,
            @PathVariable Long personaId,
            @AuthenticationPrincipal AuthenticatedUser user
            ) {
        StorySessionsDTO storySessionsDTO = StorySessionsDTO.builder()
                .userId(user.getId())
                .worldId(worldId)
                .personaId(personaId)
                .build();
        Long storySessionId = storySessionsService.createStorySession(storySessionsDTO);
        return "redirect:/sessions/" + storySessionId + "/play";
    }
}
